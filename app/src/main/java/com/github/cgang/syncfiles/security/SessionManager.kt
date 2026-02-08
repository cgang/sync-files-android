package com.github.cgang.syncfiles.security

import android.content.Context
import android.content.SharedPreferences
import com.github.cgang.syncfiles.constants.Config
import com.github.cgang.syncfiles.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val context: Context,
    private val credentialManager: CredentialManager
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "session_prefs",
        Context.MODE_PRIVATE
    )

    private val _session = MutableStateFlow<User?>(null)
    val session: StateFlow<User?> = _session.asStateFlow()

    var serverUrl: String
        get() = prefs.getString(Config.KEY_SERVER_URL, Config.DEFAULT_SERVER_URL) ?: Config.DEFAULT_SERVER_URL
        set(value) = prefs.edit().putString(Config.KEY_SERVER_URL, value).apply()

    init {
        restoreSession()
    }

    fun setCurrentUser(user: User) {
        _session.value = user
        prefs.edit()
            .putString(Config.KEY_USERNAME, user.username)
            .apply()
    }

    fun clearCurrentUser() {
        _session.value = null
        prefs.edit()
            .remove(Config.KEY_USERNAME)
            .apply()
    }

    private fun restoreSession() {
        val username = prefs.getString(Config.KEY_USERNAME, null)
        if (username != null) {
        }
    }

    fun getCurrentUser(): User? {
        return _session.value
    }
}
