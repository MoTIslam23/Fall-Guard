from fastapi import FastAPI, HTTPException

from models import (
    UserSetupRequest,
    DeviceRegistration,
    StartCountdownRequest,
    CancelCountdownRequest,
    CreateIncidentRequest,
    AcceptIncidentRequest,
    UserCheckInRequest,
)
from store import DEVICES
from user_service import create_or_update_user, get_monitoring_status
from incident_service import (
    start_countdown,
    cancel_countdown,
    send_alerts,
    accept_alert,
    submit_user_check_in,
    escalate_to_ems,
    get_active_incident_for_user,
    get_incident,
)

# ---------------------------------------------------------
# Main FastAPI application
# These routes map directly to the Figma wireframes.
# ---------------------------------------------------------

app = FastAPI(title="FallGuard API")

@app.get("/")
def root():
    return {"message": "FallGuard backend is running"}

# ---------------------------------------------------------
# ONBOARDING / HOME / SETTINGS
# ---------------------------------------------------------

@app.post("/users/setup")
def setup_user(payload: UserSetupRequest):
    """
    Used by:
    - profile setup screen
    - emergency contacts setup
    - EMS mode toggle
    """
    return create_or_update_user(payload)

@app.get("/users/{user_id}/home")
def get_home_state(user_id: str):
    """
    Used by:
    - home screen
    - settings summary screen
    """
    try:
        current_incident = get_active_incident_for_user(user_id)
        return get_monitoring_status(user_id, current_incident)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))

@app.post("/devices/register")
def register_device(payload: DeviceRegistration):
    """
    Registers user or contact devices for push notifications.
    """
    device_id = f"{payload.owner_type}:{payload.owner_id}:{payload.push_token}"
    DEVICES[device_id] = payload.model_dump()
    return {"success": True, "device_id": device_id}

# ---------------------------------------------------------
# FALL DETECTED / COUNTDOWN
# ---------------------------------------------------------

@app.post("/fall/start-countdown")
async def api_start_countdown(payload: StartCountdownRequest):
    """
    Called when sensors detect a likely fall.
    App then shows countdown screen.
    """
    try:
        return await start_countdown(payload.user_id, payload.seconds)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/fall/cancel")
async def api_cancel_countdown(payload: CancelCountdownRequest):
    """
    Called when the user cancels before alerting contacts.
    """
    incident = await cancel_countdown(payload.user_id)
    if not incident:
        raise HTTPException(status_code=404, detail="No active incident found")
    return incident

# ---------------------------------------------------------
# INCIDENT CREATION / ALERTING
# ---------------------------------------------------------

@app.post("/incidents")
async def create_incident(payload: CreateIncidentRequest):
    """
    Used when:
    - countdown expires
    - user presses 'I need help' immediately
    """
    try:
        incident = await start_countdown(payload.user_id, 0)
        incident["trigger_source"] = payload.trigger_source
        return await send_alerts(incident["incident_id"])
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/incidents/{incident_id}")
def read_incident(incident_id: str):
    """
    Used by:
    - alert sent screen
    - contact alert screen
    - resolved screen
    """
    try:
        return get_incident(incident_id)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))

@app.post("/incidents/{incident_id}/send-alerts")
async def api_send_alerts(incident_id: str):
    """
    Called when countdown expires on the phone.
    """
    try:
        return await send_alerts(incident_id)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/incidents/{incident_id}/accept")
async def api_accept_alert(incident_id: str, payload: AcceptIncidentRequest):
    """
    Called by the emergency contact flow.
    """
    try:
        return await accept_alert(incident_id, payload.contact_id)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

# ---------------------------------------------------------
# USER CHECK-IN / EMS ESCALATION
# ---------------------------------------------------------

@app.post("/incidents/{incident_id}/check-in")
async def api_check_in(incident_id: str, payload: UserCheckInRequest):
    """
    Called from the user check-in screen.
    """
    try:
        return await submit_user_check_in(incident_id, payload.response)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/incidents/{incident_id}/escalate-ems")
async def api_escalate_ems(incident_id: str):
    """
    Manual EMS escalation endpoint.
    """
    try:
        return await escalate_to_ems(incident_id)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))