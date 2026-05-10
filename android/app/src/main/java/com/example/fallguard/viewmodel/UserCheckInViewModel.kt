package com.example.fallguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallguard.data.UiStateRepository
import com.example.fallguard.network.IncidentResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ---------------------------------------------------------
// ViewModel for user check-in screen.
// ---------------------------------------------------------

class UserCheckInViewModel(
    private val repo: UiStateRepository,
    private val localStore: com.example.fallguard.data.LocalUserStore
) : ViewModel() {

    private val _incident = MutableStateFlow<IncidentResponse?>(null)
    val incident: StateFlow<IncidentResponse?> = _incident

    fun userIsOkay(incidentId: String) {
        viewModelScope.launch {
            try { _incident.value = repo.submitUserOkay(incidentId) } catch (_: Exception) {}
        }
    }

    fun userNeedsHelp(incidentId: String) {
        viewModelScope.launch {
            try { _incident.value = repo.submitUserNeedsHelp(incidentId) } catch (_: Exception) {}
        }
    }

    fun loadIncident(incidentId: String) {
        viewModelScope.launch {
            try { _incident.value = repo.fetchIncident(incidentId) } catch (_: Exception) {}
        }
    }
}