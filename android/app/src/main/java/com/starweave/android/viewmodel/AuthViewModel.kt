package com.starweave.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starweave.android.api.ApiClient
import com.starweave.android.model.User
import com.starweave.android.util.PrefsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLoginEffect: Boolean = false,
    val showLogoutEffect: Boolean = false
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val api = ApiClient.getService()
    private val prefs = PrefsManager(app)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // Restore session
        viewModelScope.launch {
            prefs.user.collect { user ->
                if (user != null && user.id > 0) {
                    _state.update { it.copy(user = user, isLoggedIn = true) }
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val resp = api.loginWithPassword(mapOf("username" to username, "password" to password))
                if (resp.isSuccess && resp.data != null) {
                    prefs.saveUser(resp.data)
                    _state.update { it.copy(user = resp.data, isLoggedIn = true, isLoading = false, showLoginEffect = true) }
                } else {
                    _state.update { it.copy(isLoading = false, error = resp.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "网络错误: ${e.localizedMessage}") }
            }
        }
    }

    fun register(username: String, nickname: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val resp = api.register(mapOf("username" to username, "nickname" to nickname, "password" to password))
                if (resp.isSuccess && resp.data != null) {
                    prefs.saveUser(resp.data)
                    _state.update { it.copy(user = resp.data, isLoggedIn = true, isLoading = false, showLoginEffect = true) }
                } else {
                    _state.update { it.copy(isLoading = false, error = resp.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "网络错误: ${e.localizedMessage}") }
            }
        }
    }

    fun logout() {
        _state.update { it.copy(showLogoutEffect = true) }
    }

    fun onLogoutEffectComplete() {
        viewModelScope.launch {
            prefs.clearUser()
            _state.update { AuthState() }
        }
    }

    fun onLoginEffectComplete() {
        _state.update { it.copy(showLoginEffect = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            prefs.saveUser(user)
            _state.update { it.copy(user = user) }
        }
    }
}
