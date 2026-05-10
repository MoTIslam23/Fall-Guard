import asyncio
from datetime import datetime, timezone
from uuid import uuid4

from store import USERS, INCIDENTS
from notification_service import send_push_to_owner

# ---------------------------------------------------------
# Incident / fall workflow service
# This file powers:
# - countdown screen
# - alert sent screen
# - contact acceptance
# - user check-in
# - EMS escalation
# ---------------------------------------------------------

USER_RESPONSE_TIMEOUT_SECONDS = 5 * 60

def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()

def get_user(user_id: str):
    user = USERS.get(user_id)
    if not user:
        raise ValueError("User not found")
    return user

def get_incident(incident_id: str):
    incident = INCIDENTS.get(incident_id)
    if not incident:
        raise ValueError("Incident not found")
    return incident

def get_active_incident_for_user(user_id: str):
    for incident in INCIDENTS.values():
        if incident["user_id"] == user_id and incident["status"] not in [
            "RESOLVED_FALSE_ALARM",
            "RESOLVED_HELP_CONFIRMED",
            "CLOSED",
        ]:
            return incident
    return None

async def start_countdown(user_id: str, seconds: int):
    """
    Called when the phone detects a possible fall.
    This creates a temporary incident in countdown state.
    """
    _ = get_user(user_id)

    incident_id = str(uuid4())
    INCIDENTS[incident_id] = {
        "incident_id": incident_id,
        "user_id": user_id,
        "status": "COUNTDOWN_ACTIVE",
        "created_at": now_iso(),
        "updated_at": now_iso(),
        "trigger_source": "AUTO_DETECTED",
        "countdown_seconds": seconds,
        "accepted_by_contact_id": None,
        "user_response": None,
        "alert_target": None,
        "contacts_status": [],
        "ems_notified": False,
    }
    return INCIDENTS[incident_id]

async def cancel_countdown(user_id: str):
    """
    Marks the active incident as a false alarm.
    Used when the user taps 'I'm okay' before alerting others.
    """
    incident = get_active_incident_for_user(user_id)
    if not incident:
        return None

    incident["status"] = "RESOLVED_FALSE_ALARM"
    incident["user_response"] = "I_AM_OK"
    incident["updated_at"] = now_iso()
    return incident

async def send_alerts(incident_id: str):
    """
    Moves a countdown incident into alerting state.
    Sends push alerts to emergency contacts in priority order.
    """
    incident = get_incident(incident_id)
    user = get_user(incident["user_id"])

    incident["status"] = "ALERTING_CONTACTS"
    incident["alert_target"] = "CONTACTS"
    incident["contacts_status"] = []

    for contact in sorted(user["emergency_contacts"], key=lambda c: c["priority_order"]):
        contact_status = {
            "contact_id": contact["contact_id"],
            "name": contact["name"],
            "priority_order": contact["priority_order"],
            "status": "PENDING",
        }
        incident["contacts_status"].append(contact_status)

        await send_push_to_owner(
            owner_type="contact",
            owner_id=contact["contact_id"],
            title="Possible Fall Detected",
            body=f"{user['name']} may have fallen. Open the app to respond.",
            data={
                "screen": "contact_alert",
                "incident_id": incident["incident_id"],
                "user_id": user["user_id"],
            },
        )

    incident["updated_at"] = now_iso()
    return incident

async def accept_alert(incident_id: str, contact_id: str):
    """
    Called when one emergency contact accepts the alert.
    After one contact accepts, the user receives a check-in prompt.
    """
    incident = get_incident(incident_id)

    if incident["status"] != "ALERTING_CONTACTS":
        raise ValueError("Incident is not currently alerting contacts")

    incident["accepted_by_contact_id"] = contact_id
    incident["status"] = "CHECK_IN_PENDING"
    incident["updated_at"] = now_iso()

    for contact_status in incident["contacts_status"]:
        if contact_status["contact_id"] == contact_id:
            contact_status["status"] = "ACCEPTED"
        elif contact_status["status"] == "PENDING":
            contact_status["status"] = "NOTIFIED"

    await send_push_to_owner(
        owner_type="user",
        owner_id=incident["user_id"],
        title="Are you okay?",
        body="An emergency contact is checking on you.",
        data={
            "screen": "user_check_in",
            "incident_id": incident["incident_id"],
        },
    )

    asyncio.create_task(wait_for_user_response(incident["incident_id"]))
    return incident

async def submit_user_check_in(incident_id: str, response: str):
    """
    Called from the user's check-in screen.
    """
    incident = get_incident(incident_id)

    incident["user_response"] = response
    incident["updated_at"] = now_iso()

    if response == "I_AM_OK":
        incident["status"] = "RESOLVED_FALSE_ALARM"
    elif response == "NEED_HELP":
        incident["status"] = "RESOLVED_HELP_CONFIRMED"

    return incident

async def escalate_to_ems(incident_id: str):
    """
    Escalates the incident to EMS mode.
    This is used if the user does not respond in time or
    if the design later wants a direct EMS fallback.
    """
    incident = get_incident(incident_id)

    incident["status"] = "ESCALATED_TO_EMS"
    incident["alert_target"] = "EMS"
    incident["ems_notified"] = True
    incident["updated_at"] = now_iso()

    await send_push_to_owner(
        owner_type="user",
        owner_id=incident["user_id"],
        title="Contacting EMS",
        body="Emergency services have been notified.",
        data={
            "screen": "ems_contacting",
            "incident_id": incident["incident_id"],
        },
    )

    return incident

async def wait_for_user_response(incident_id: str):
    """
    Background timeout after contact acceptance.
    If the user does not respond in 5 minutes, the backend escalates.
    """
    await asyncio.sleep(USER_RESPONSE_TIMEOUT_SECONDS)

    incident = INCIDENTS.get(incident_id)
    if not incident:
        return

    if incident["status"] == "CHECK_IN_PENDING" and incident["user_response"] is None:
        await escalate_to_ems(incident_id)