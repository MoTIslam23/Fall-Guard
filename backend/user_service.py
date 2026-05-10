from datetime import datetime, timezone
from store import USERS

# ---------------------------------------------------------
# User/profile service
# Handles onboarding, profile setup, contact setup, and
# home screen monitoring summary.
# ---------------------------------------------------------

def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()

def create_or_update_user(payload):
    USERS[payload.user_id] = {
        "user_id": payload.user_id,
        "name": payload.name,
        "monitoring_enabled": True,
        "permissions_granted": payload.permissions_granted,
        "ems_mode_enabled": payload.ems_mode_enabled,
        "emergency_contacts": [c.model_dump() for c in payload.emergency_contacts],
        "created_at": now_iso(),
        "updated_at": now_iso(),
    }
    return USERS[payload.user_id]

def get_user(user_id: str):
    user = USERS.get(user_id)
    if not user:
        raise ValueError("User not found")
    return user

def get_monitoring_status(user_id: str, current_incident=None):
    user = get_user(user_id)

    return {
        "user_id": user["user_id"],
        "monitoring_enabled": user["monitoring_enabled"],
        "permissions_granted": user["permissions_granted"],
        "ems_mode_enabled": user["ems_mode_enabled"],
        "sensors_active": user["monitoring_enabled"] and user["permissions_granted"],
        "notifications_enabled": True,
        "emergency_contacts_count": len(user["emergency_contacts"]),
        "current_incident_id": current_incident["incident_id"] if current_incident else None,
        "current_incident_status": current_incident["status"] if current_incident else None,
    }