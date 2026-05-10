package com.example.fallguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallguard.data.LocalUserStore
import com.example.fallguard.data.UiStateRepository
import com.example.fallguard.network.IncidentResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FallCountdownViewModel(
    private val repo: UiStateRepository,
    private val localStore: LocalUserStore
) : ViewModel() {

    private val _incident = MutableStateFlow<IncidentResponse?>(null)
    val incident: StateFlow<IncidentResponse?> = _incident.asStateFlow()

    fun startCountdown() {
        val userId = localStore.getUserId()
        viewModelScope.launch {
            try {
                _incident.value = repo.startFallCountdown(userId)
            } catch (_: Exception) { /* offline — UI handles countdown locally */ }
        }
    }

    fun cancel() {
        val userId = localStore.getUserId()
        viewModelScope.launch {
            try {
                _incident.value = repo.cancelFallAlert(userId)
            } catch (_: Exception) { }
        }
    }

    fun immediateHelp() {
        val userId = localStore.getUserId()
        viewModelScope.launch {
            try {
                _incident.value = repo.createImmediateHelpIncident(userId)
            } catch (_: Exception) { }
        }
    }

    fun sendAlerts(incidentId: String) {
        viewModelScope.launch {
            try {
                _incident.value = repo.sendAlertsAfterCountdown(incidentId)
            } catch (_: Exception) { }
        }
    }
}
