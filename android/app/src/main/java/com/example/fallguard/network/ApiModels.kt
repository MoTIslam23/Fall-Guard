package com.example.fallguard.network

// ---------------------------------------------------------
// Network DTOs used by Retrofit to communicate with backend.
// These models match the backend JSON exactly.
// ---------------------------------------------------------

data class EmergencyContactDto(
    val contact_id: String,
    val name: String,
    val phone_number: String? = null,
    val priority_order: Int
)

data class UserSetupRequest(
    val user_id: String,
    val name: String,
    val permissions_granted: Boolean,
    val ems_mode_enabled: Boolean,
    val emergency_contacts: List<EmergencyContactDto>
)

data class DeviceRegistration(
    val owner_type: String,
    val owner_id: String,
    val push_token: String
)

data class StartCountdownRequest(
    val user_id: String,
    val seconds: Int = 15
)

data class CancelCountdownRequest(
    val user_id: String
)

data class CreateIncidentRequest(
    val user_id: String,
    val trigger_source: String
)

data class AcceptIncidentRequest(
    val contact_id: String
)

data class UserCheckInRequest(
    val response: String
)

data class ContactAlertStatusDto(
    val contact_id: String,
    val name: String,
    val priority_order: Int,
    val status: String
)

data class IncidentResponse(
    val incident_id: String,
    val user_id: String,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val trigger_source: String,
    val countdown_seconds: Int?,
    val accepted_by_contact_id: String?,
    val user_response: String?,
    val alert_target: String?,
    val contacts_status: List<ContactAlertStatusDto>,
    val ems_notified: Boolean
)

data class HomeStateResponse(
    val user_id: String,
    val monitoring_enabled: Boolean,
    val permissions_granted: Boolean,
    val ems_mode_enabled: Boolean,
    val sensors_active: Boolean,
    val notifications_enabled: Boolean,
    val emergency_contacts_count: Int,
    val current_incident_id: String?,
    val current_incident_status: String?
)