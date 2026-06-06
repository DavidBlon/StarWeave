package com.starweave.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starweave.android.api.ApiClient
import com.starweave.android.model.Message
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LaunchState(
    val myMeteors: List<Message> = emptyList(),
    val isPublishing: Boolean = false,
    val publishedMeteor: Message? = null,
    val showHealingEcho: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null
)

class LaunchViewModel : ViewModel() {
    private val api = ApiClient.getService()

    private val _state = MutableStateFlow(LaunchState())
    val state: StateFlow<LaunchState> = _state.asStateFlow()

    fun loadMyMeteors(userId: Long) {
        viewModelScope.launch {
            try {
                val resp = api.getUserMeteors(userId)
                if (resp.isSuccess) {
                    _state.update { it.copy(myMeteors = resp.data ?: emptyList()) }
                }
            } catch (_: Exception) {}
        }
    }

    fun publishMeteor(userId: Long, content: String) {
        viewModelScope.launch {
            _state.update { it.copy(isPublishing = true, toastMessage = "正在温柔审核...") }
            try {
                val resp = api.publishMeteor(mapOf("userId" to userId.toString(), "content" to content))
                if (resp.isSuccess && resp.data != null) {
                    val meteor = resp.data
                    if (meteor.status == "rejected") {
                        _state.update { it.copy(isPublishing = false, error = meteor.reviewReason ?: "内容未通过审核，请修改后重试", toastMessage = null) }
                    } else {
                        _state.update {
                            it.copy(
                                isPublishing = false,
                                publishedMeteor = meteor,
                                showHealingEcho = !meteor.healingMessage.isNullOrEmpty(),
                                toastMessage = null
                            )
                        }
                        loadMyMeteors(userId)
                    }
                } else {
                    _state.update { it.copy(isPublishing = false, error = resp.message, toastMessage = null) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isPublishing = false, error = "网络错误", toastMessage = null) }
            }
        }
    }

    fun dismissHealingEcho() {
        _state.update { it.copy(showHealingEcho = false, publishedMeteor = null) }
    }

    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
