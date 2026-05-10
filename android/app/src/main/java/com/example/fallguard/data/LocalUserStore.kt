package com.example.fallguard.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ---------------------------------------------------------
// Persists user profile and emergency contacts locally
// using SharedPreferences. Allows the app to work fully
// offline and avoids requiring the backend for basic use.
// ---------------------------------------------------------

data class LocalContact(
    val id: String,
    val name: String,
    val phone: String,
    val priority: Int
)

class LocalUserStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fallguard_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    // ---- User identity ----

    fun getUserId(): String {
        return prefs.getString("user_id", null) ?: run {
            val id = "user-${System.currentTimeMillis()}"
            prefs.edit().putString("user_id", id).apply()
            id
        }
    }

    fun getUserName(): String = prefs.getString("user_name", "") ?: ""

    fun setUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    // ---- EMS mode ----

    fun isEmsModeEnabled(): Boolean = prefs.getBoolean("ems_mode", false)

    fun setEmsMode(enabled: Boolean) {
        prefs.edit().putBoolean("ems_mode", enabled).apply()
    }

    // ---- Emergency contacts ----

    fun getContacts(): List<LocalContact> {
        val json = prefs.getString("contacts", null) ?: return emptyList()
        val type = object : TypeToken<List<LocalContact>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun setContacts(contacts: List<LocalContact>) {
        prefs.edit().putString("contacts", gson.toJson(contacts)).apply()
    }

    fun addContact(name: String, phone: String) {
        val existing = getContacts().toMutableList()
        val newContact = LocalContact(
            id = "contact-${System.currentTimeMillis()}",
            name = name,
            phone = phone,
            priority = existing.size + 1
        )
        existing.add(newContact)
        setContacts(existing)
    }

    fun removeContact(id: String) {
        val updated = getContacts()
            .filter { it.id != id }
            .mapIndexed { index, c -> c.copy(priority = index + 1) }
        setContacts(updated)
    }

    // ---- Onboarding state ----

    fun isOnboardingComplete(): Boolean = prefs.getBoolean("onboarding_done", false)

    fun markOnboardingComplete() {
        prefs.edit().putBoolean("onboarding_done", true).apply()
    }

    fun resetAllData() {
        prefs.edit().clear().apply()
    }
}
