package com.example.fallguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallguard.data.UiStateRepository
import com.example.fallguard.network.IncidentResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactAlertViewModel(
    private val repo: UiStateRepository
) : ViewModel() {

    private val _incident = MutableStateFlow<IncidentResponse?>(null)
    val incident: StateFlow<IncidentResponse?> = _incident.asStateFlow()

    fun loadIncident(incidentId: String) {
        viewModelScope.launch {
            try { _incident.value = repo.fetchIncident(incidentId) } catch (_: Exception) {}
        }
    }

    fun accept(incidentId: String, contactId: String) {
        viewModelScope.launch {
            try { _incident.value = repo.acceptContactAlert(incidentId, contactId) } catch (_: Exception) {}
        }
    }
}