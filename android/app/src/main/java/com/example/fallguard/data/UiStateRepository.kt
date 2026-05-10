package com.example.fallguard.data

import com.example.fallguard.network.*

// ---------------------------------------------------------
// Repository layer used by ViewModels.
// Keeps network calls out of UI files.
// ---------------------------------------------------------

class UiStateRepository(
    private val api: BackendApi
) {

    suspend fun setupUserProfile(request: UserSetupRequest) = api.setupUser(request)

    suspend fun fetchHomeState(userId: String): HomeStateResponse {
        return api.getHomeState(userId)
    }

    suspend fun startFallCountdown(userId: String, seconds: Int = 15): IncidentResponse {
        return api.startCountdown(StartCountdownRequest(user_id = userId, seconds = seconds))
    }

    suspend fun cancelFallAlert(userId: String): IncidentResponse {
        return api.cancelCountdown(CancelCountdownRequest(user_id = userId))
    }

    suspend fun createImmediateHelpIncident(userId: String): IncidentResponse {
        return api.createIncident(
            CreateIncidentRequest(
                user_id = userId,
                trigger_source = "MANUAL_HELP_BUTTON"
            )
        )
    }

    suspend fun sendAlertsAfterCountdown(incidentId: String): IncidentResponse {
        return api.sendAlerts(incidentId)
    }

    suspend fun fetchIncident(incidentId: String): IncidentResponse {
        return api.getIncident(incidentId)
    }

    suspend fun acceptContactAlert(incidentId: String, contactId: String): IncidentResponse {
        return api.acceptIncident(
            incidentId,
            AcceptIncidentRequest(contact_id = contactId)
        )
    }

    suspend fun submitUserOkay(incidentId: String): IncidentResponse {
        return api.submitCheckIn(
            incidentId,
            UserCheckInRequest(response = "I_AM_OK")
        )
    }

    suspend fun submitUserNeedsHelp(incidentId: String): IncidentResponse {
        return api.submitCheckIn(
            incidentId,
            UserCheckInRequest(response = "NEED_HELP")
        )
    }
}