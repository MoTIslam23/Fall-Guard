package com.example.fallguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallguard.data.LocalContact
import com.example.fallguard.data.LocalUserStore
import com.example.fallguard.data.UiStateRepository
import com.example.fallguard.network.EmergencyContactDto
import com.example.fallguard.network.UserSetupRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repo: UiStateRepository,
    private val localStore: LocalUserStore
) : ViewModel() {

    private val _setupComplete = MutableStateFlow(false)
    val setupComplete: StateFlow<Boolean> = _setupComplete.asStateFlow()

    fun saveProfile(
        name: String,
        emsModeEnabled: Boolean,
        contacts: List<LocalContact>
    ) {
        // Persist locally — works offline
        localStore.setUserName(name)
        localStore.setEmsMode(emsModeEnabled)
        localStore.setContacts(contacts)
        localStore.markOnboardingComplete()

        val userId = localStore.getUserId()

        // Also push to backend if reachable
        viewModelScope.launch {
            try {
                repo.setupUserProfile(
                    UserSetupRequest(
                        user_id = userId,
                        name = name,
                        permissions_granted = true,
                        ems_mode_enabled = emsModeEnabled,
                        emergency_contacts = contacts.map { c ->
                            EmergencyContactDto(
                                contact_id = c.id,
                                name = c.name,
                                phone_number = c.phone,
                                priority_order = c.priority
                            )
                        }
                    )
                )
            } catch (_: Exception) { /* offline — local save is enough */ }
            _setupComplete.value = true
        }
    }
}
