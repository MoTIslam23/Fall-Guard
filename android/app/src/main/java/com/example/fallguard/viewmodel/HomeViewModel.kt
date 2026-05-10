package com.example.fallguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallguard.data.LocalContact
import com.example.fallguard.data.LocalUserStore
import com.example.fallguard.data.UiStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val contacts: List<LocalContact> = emptyList(),
    val emsModeEnabled: Boolean = false,
    val sensorsActive: Boolean = true,
    val notificationsEnabled: Boolean = true,
)

class HomeViewModel(
    private val repo: UiStateRepository,
    private val localStore: LocalUserStore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        loadLocalState()
    }

    private fun loadLocalState() {
        _state.value = HomeUiState(
            userName = localStore.getUserName(),
            contacts = localStore.getContacts(),
            emsModeEnabled = localStore.isEmsModeEnabled(),
        )
        // Also try fetching from backend (optional — silently ignored if offline)
        viewModelScope.launch {
            try {
                val remote = repo.fetchHomeState(localStore.getUserId())
                _state.value = _state.value.copy(
                    notificationsEnabled = remote.notifications_enabled,
                    sensorsActive = remote.sensors_active,
                )
            } catch (_: Exception) { /* offline — use local */ }
        }
    }

    fun reload() = loadLocalState()

    fun resetProfile() {
        localStore.resetAllData()
    }
}
