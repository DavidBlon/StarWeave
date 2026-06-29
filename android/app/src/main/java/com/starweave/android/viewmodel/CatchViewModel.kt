package com.starweave.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starweave.android.api.ApiClient
import com.starweave.android.model.Message
import com.starweave.android.model.Wish
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CatchState(
    val currentMeteor: Message? = null,
    val wishes: List<Wish> = emptyList(),
    val catchHistory: List<Message> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val hasLoadedHistory: Boolean = false,
    val historyUserId: Long? = null,
    val isLoading: Boolean = false,
    val isCatching: Boolean = false,
    val isSendingWish: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null
)

class CatchViewModel : ViewModel() {
    private val api = ApiClient.getService()

    private val _state = MutableStateFlow(CatchState())
    val state: StateFlow<CatchState> = _state.asStateFlow()

    fun catchRandomMeteor(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isCatching = true, error = null) }
            try {
                val resp = api.getRandomMeteor(userId)
                if (resp.isSuccess && resp.data != null) {
                    val meteor = resp.data
                    // Record catch — if it fails (already caught by someone else), show error
                    try {
                        val catchResp = api.catchMeteor(meteor.id, mapOf("userId" to userId.toString()))
                        if (catchResp.isSuccess) {
                            _state.update { it.copy(currentMeteor = meteor, isCatching = false) }
                            loadWishes(meteor.id)
                        } else {
                            _state.update { it.copy(isCatching = false, error = catchResp.message.ifEmpty { "这颗流星已经被别人捞走了" }) }
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(isCatching = false, error = "捞取失败，请重试") }
                    }
                } else {
                    _state.update { it.copy(isCatching = false, error = resp.message.ifEmpty { "星海中暂时没有流星..." }) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isCatching = false, error = "网络错误") }
            }
        }
    }

    fun loadWishes(meteorId: Long) {
        viewModelScope.launch {
            try {
                val resp = api.getWishes(meteorId)
                if (resp.isSuccess) {
                    _state.update { it.copy(wishes = resp.data ?: emptyList()) }
                }
            } catch (_: Exception) {}
        }
    }

    fun makeWish(meteorId: Long, userId: Long, content: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSendingWish = true) }
            val optimisticId = -System.currentTimeMillis()
            try {
                // Optimistic: add to list immediately
                val optimistic = Wish(
                    id = optimisticId,
                    meteorId = meteorId,
                    userId = userId,
                    content = content,
                    status = "pending",
                    replierNickname = "..."
                )
                _state.update { it.copy(wishes = it.wishes + optimistic) }

                val resp = api.makeWish(meteorId, mapOf("userId" to userId.toString(), "content" to content))
                if (resp.isSuccess) {
                    _state.update { it.copy(isSendingWish = false, toastMessage = "回复已发送") }
                    // Reload to get real data
                    loadWishes(meteorId)
                } else {
                    // Remove optimistic wish on server rejection
                    _state.update { st -> st.copy(wishes = st.wishes.filter { it.id != optimisticId }, isSendingWish = false, error = resp.message.ifEmpty { "发送失败" }) }
                }
            } catch (e: Exception) {
                // Remove optimistic wish on network error
                _state.update { st -> st.copy(wishes = st.wishes.filter { it.id != optimisticId }, isSendingWish = false, error = "发送失败") }
            }
        }
    }

    fun loadCatchHistory(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingHistory = true, historyUserId = userId) }
            try {
                val resp = api.getCaughtMeteors(userId)
                if (resp.isSuccess) {
                    _state.update {
                        it.copy(
                            catchHistory = resp.data ?: emptyList(),
                            isLoadingHistory = false,
                            hasLoadedHistory = true,
                            historyUserId = userId
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoadingHistory = false,
                            hasLoadedHistory = true,
                            historyUserId = userId,
                            error = resp.message
                        )
                    }
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isLoadingHistory = false,
                        hasLoadedHistory = true,
                        historyUserId = userId
                    )
                }
            }
        }
    }

    fun clearCurrentMeteor() {
        _state.update { it.copy(currentMeteor = null, wishes = emptyList()) }
    }

    fun clearToast() { _state.update { it.copy(toastMessage = null) } }
    fun clearError() { _state.update { it.copy(error = null) } }
}
