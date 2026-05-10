package com.example.fallguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fallguard.data.LocalUserStore
import com.example.fallguard.data.UiStateRepository
import com.example.fallguard.network.ApiProvider

// ---------------------------------------------------------
// Provides ViewModels with their required dependencies.
// Uses the app's shared LocalUserStore and the network repo.
// ---------------------------------------------------------

class ViewModelFactory(
    private val localStore: LocalUserStore
) : ViewModelProvider.Factory {

    private val repo by lazy {
        UiStateRepository(ApiProvider.api)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repo, localStore) as T
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(repo, localStore) as T
            modelClass.isAssignableFrom(FallCountdownViewModel::class.java) ->
                FallCountdownViewModel(repo, localStore) as T
            modelClass.isAssignableFrom(ContactAlertViewModel::class.java) ->
                ContactAlertViewModel(repo) as T
            modelClass.isAssignableFrom(UserCheckInViewModel::class.java) ->
                UserCheckInViewModel(repo, localStore) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
