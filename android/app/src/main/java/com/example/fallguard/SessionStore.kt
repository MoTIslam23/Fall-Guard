package com.example.fallguard

// ---------------------------------------------------------
// Simple shared in-memory session state for prototype use.
// In a more complete app, this can move to DataStore.
// ---------------------------------------------------------

object SessionStore {
    var currentUserId: String? = "demo-user-1"
    var currentContactId: String? = null
    var currentRole: String = "user" // "user" or "contact"
}