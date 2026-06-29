package com.starweave.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starweave.android.api.ApiClient
import com.starweave.android.model.Captcha
import com.starweave.android.model.User
import com.starweave.android.util.PrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val captcha: Captcha? = null,
    val captchaLoading: Boolean = false,
    val showLoginEffect: Boolean = false,
    val showLogoutEffect: Boolean = false
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val api = ApiClient.getService()
    private val prefs = PrefsManager(app)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.user.collect { user ->
                if (user != null && user.id > 0) {
                    _state.update { it.copy(user = user, isLoggedIn = true) }
                }
            }
        }
        // 恢复 JWT token
        viewModelScope.launch {
            prefs.token.collect { t ->
                if (t != null) ApiClient.token = t
            }
        }
        refreshCaptcha()
    }

    fun refreshCaptcha() {
        viewModelScope.launch {
            _state.update { it.copy(captchaLoading = true) }
            try {
                val resp = api.getCaptcha()
                _state.update {
                    it.copy(
                        captcha = if (resp.isSuccess) resp.data else null,
                        captchaLoading = false
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(captcha = null, captchaLoading = false) }
            }
        }
    }

    fun login(username: String, password: String, captcha: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val resp = api.loginWithPassword(
                    mapOf(
                        "username" to username,
                        "password" to password,
                        "captchaId" to _state.value.captcha?.captchaId.orEmpty(),
                        "captcha" to captcha
                    )
                )
                if (resp.isSuccess && resp.data != null) {
                    val user = resp.data.user
                    val token = resp.data.token
                    if (user != null) prefs.saveUser(user)
                    if (token != null) {
                        prefs.saveToken(token)
                        ApiClient.token = token
                    }
                    _state.update {
                        it.copy(
                            user = user,
                            isLoggedIn = true,
                            isLoading = false,
                            showLoginEffect = true
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = resp.message) }
                    refreshCaptcha()
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "网络错误: ${e.localizedMessage}") }
                refreshCaptcha()
            }
        }
    }

    fun register(username: String, nickname: String, password: String, captcha: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val resp = api.register(
                    mapOf(
                        "username" to username,
                        "nickname" to nickname,
                        "password" to password,
                        "captchaId" to _state.value.captcha?.captchaId.orEmpty(),
                        "captcha" to captcha
                    )
                )
                if (resp.isSuccess && resp.data != null) {
                    val user = resp.data.user
                    val token = resp.data.token
                    if (user != null) prefs.saveUser(user)
                    if (token != null) {
                        prefs.saveToken(token)
                        ApiClient.token = token
                    }
                    _state.update {
                        it.copy(
                            user = user,
                            isLoggedIn = true,
                            isLoading = false,
                            showLoginEffect = true
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = resp.message) }
                    refreshCaptcha()
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "网络错误: ${e.localizedMessage}") }
                refreshCaptcha()
            }
        }
    }

    fun logout() {
        // 通知服务端使当前 token 失效（fire-and-forget）
        viewModelScope.launch {
            try {
                api.logout()
            } catch (_: Exception) {
                // 网络错误不阻塞退出
            }
        }
        _state.update { it.copy(showLogoutEffect = true) }
    }

    fun onLogoutEffectComplete() {
        viewModelScope.launch {
            prefs.clearUser()
            ApiClient.token = null
            _state.update { AuthState() }
            refreshCaptcha()
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
