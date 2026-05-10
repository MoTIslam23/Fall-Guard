package com.example.fallguard.actions

import com.example.fallguard.SessionStore
import com.example.fallguard.network.ApiProvider
import com.example.fallguard.network.AcceptIncidentRequest
import com.example.fallguard.network.CancelCountdownRequest
import com.example.fallguard.network.UserCheckInRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ---------------------------------------------------------
// Reusable action helpers.
// Keeps button handlers simple in UI screens.
// ---------------------------------------------------------

object IncidentActions {

    suspend fun cancelAlertForCurrentUser(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val userId = SessionStore.currentUserId ?: return@withContext false
            ApiProvider.api.cancelCountdown(CancelCountdownRequest(user_id = userId))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun acceptAsCurrentContact(incidentId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val contactId = SessionStore.currentContactId ?: return@withContext false
            ApiProvider.api.acceptIncident(
                incidentId,
                AcceptIncidentRequest(contact_id = contactId)
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun respondOkay(incidentId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            ApiProvider.api.submitCheckIn(
                incidentId,
                UserCheckInRequest(response = "I_AM_OK")
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun respondNeedHelp(incidentId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            ApiProvider.api.submitCheckIn(
                incidentId,
                UserCheckInRequest(response = "NEED_HELP")
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}