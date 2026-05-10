from pydantic import BaseModel, Field
from typing import List, Optional, Literal

# ---------------------------------------------------------
# Shared backend models
# These models shape the JSON data returned to the mobile UI.
# ---------------------------------------------------------

IncidentStatus = Literal[
    "COUNTDOWN_ACTIVE",
    "ALERTING_CONTACTS",
    "CHECK_IN_PENDING",
    "RESOLVED_FALSE_ALARM",
    "RESOLVED_HELP_CONFIRMED",
    "ESCALATED_TO_EMS",
    "CLOSED",
]

UserResponseType = Literal["I_AM_OK", "NEED_HELP"]
TriggerSourceType = Literal["AUTO_DETECTED", "MANUAL_HELP_BUTTON"]
AlertTargetType = Literal["CONTACTS", "EMS"]

class EmergencyContact(BaseModel):
    contact_id: str
    name: str
    phone_number: Optional[str] = None
    priority_order: int

class UserSetupRequest(BaseModel):
    user_id: str
    name: str
    permissions_granted: bool
    ems_mode_enabled: bool = False
    emergency_contacts: List[EmergencyContact] = Field(default_factory=list)

class DeviceRegistration(BaseModel):
    owner_type: Literal["user", "contact"]
    owner_id: str
    push_token: str

class StartCountdownRequest(BaseModel):
    user_id: str
    seconds: int = 15

class CancelCountdownRequest(BaseModel):
    user_id: str

class CreateIncidentRequest(BaseModel):
    user_id: str
    trigger_source: TriggerSourceType = "AUTO_DETECTED"

class AcceptIncidentRequest(BaseModel):
    contact_id: str

class UserCheckInRequest(BaseModel):
    response: UserResponseType

class ContactAlertStatus(BaseModel):
    contact_id: str
    name: str
    priority_order: int
    status: Literal["PENDING", "ACCEPTED", "NOTIFIED", "SKIPPED"]

class IncidentRecord(BaseModel):
    incident_id: str
    user_id: str
    status: IncidentStatus
    created_at: str
    updated_at: str
    trigger_source: TriggerSourceType
    countdown_seconds: Optional[int] = None
    accepted_by_contact_id: Optional[str] = None
    user_response: Optional[UserResponseType] = None
    alert_target: Optional[AlertTargetType] = None
    contacts_status: List[ContactAlertStatus] = Field(default_factory=list)
    ems_notified: bool = False

class HomeStateResponse(BaseModel):
    user_id: str
    monitoring_enabled: bool
    permissions_granted: bool
    ems_mode_enabled: bool
    sensors_active: bool
    notifications_enabled: bool
    emergency_contacts_count: int
    current_incident_id: Optional[str] = None
    current_incident_status: Optional[str] = None