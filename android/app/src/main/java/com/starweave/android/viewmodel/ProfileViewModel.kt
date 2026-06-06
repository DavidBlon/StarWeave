package com.starweave.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starweave.android.api.ApiClient
import com.starweave.android.model.Message
import com.starweave.android.model.User
import com.starweave.android.model.UserStats
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileState(
    val stats: UserStats? = null,
    val caughtMeteors: List<Message> = emptyList(),
    val publishedMeteors: List<Message> = emptyList(),
    val userWishes: List<Map<String, Any>> = emptyList(),
    val showOverlay: String? = null, // "caught", "published", "wishes"
    val overlayLoading: Boolean = false,
    val changePasswordError: String? = null,
    val changePasswordSuccess: Boolean = false,
    val toastMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val api = ApiClient.getService()

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun loadStats(userId: Long) {
        viewModelScope.launch {
            try {
                val resp = api.getUserStats(userId)
                if (resp.isSuccess && resp.data != null) {
                    _state.update { it.copy(stats = resp.data) }
                }
            } catch (_: Exception) {}
        }
    }

    fun updateProfile(userId: Long, nickname: String?, bio: String?, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val body = mutableMapOf<String, String>()
                nickname?.let { body["nickname"] = it }
                bio?.let { body["bio"] = it }
                val resp = api.updateProfile(userId, body)
                if (resp.isSuccess && resp.data != null) {
                    onResult(resp.data)
                    _state.update { it.copy(toastMessage = "已保存") }
                } else {
                    onResult(null)
                    _state.update { it.copy(toastMessage = "保存失败") }
                }
            } catch (e: Exception) {
                onResult(null)
                _state.update { it.copy(toastMessage = "网络错误") }
            }
        }
    }

    fun changePassword(userId: Long, oldPwd: String, newPwd: String) {
        viewModelScope.launch {
            try {
                val resp = api.changePassword(userId, mapOf("oldPassword" to oldPwd, "newPassword" to newPwd))
                if (resp.isSuccess) {
                    _state.update { it.copy(changePasswordSuccess = true, changePasswordError = null, toastMessage = "密码已修改") }
                } else {
                    _state.update { it.copy(changePasswordError = resp.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(changePasswordError = "网络错误") }
            }
        }
    }

    fun loadCaughtMeteors(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(overlayLoading = true, showOverlay = "caught") }
            try {
                val resp = api.getCaughtMeteors(userId)
                if (resp.isSuccess) _state.update { it.copy(caughtMeteors = resp.data ?: emptyList(), overlayLoading = false) }
            } catch (_: Exception) { _state.update { it.copy(overlayLoading = false) } }
        }
    }

    fun loadPublishedMeteors(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(overlayLoading = true, showOverlay = "published") }
            try {
                val resp = api.getUserMeteors(userId)
                if (resp.isSuccess) _state.update { it.copy(publishedMeteors = resp.data ?: emptyList(), overlayLoading = false) }
            } catch (_: Exception) { _state.update { it.copy(overlayLoading = false) } }
        }
    }

    fun loadUserWishes(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(overlayLoading = true, showOverlay = "wishes") }
            try {
                val resp = api.getUserWishes(userId)
                if (resp.isSuccess) _state.update { it.copy(userWishes = resp.data ?: emptyList(), overlayLoading = false) }
            } catch (_: Exception) { _state.update { it.copy(overlayLoading = false) } }
        }
    }

    fun closeOverlay() { _state.update { it.copy(showOverlay = null) } }
    fun clearToast() { _state.update { it.copy(toastMessage = null) } }
    fun resetPasswordState() { _state.update { it.copy(changePasswordError = null, changePasswordSuccess = false) } }
}
