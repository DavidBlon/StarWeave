package com.starweave.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starweave.android.api.ApiClient
import com.starweave.android.model.Message
import com.starweave.android.model.User
import com.starweave.android.model.Wish
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminState(
    val activeTab: Int = 0, // 0=pending, 1=pendingWishes, 2=allMeteors, 3=allWishes, 4=users
    val pendingMeteors: List<Message> = emptyList(),
    val pendingWishes: List<Wish> = emptyList(),
    val allMeteors: List<Message> = emptyList(),
    val allWishes: List<Wish> = emptyList(),
    val users: List<User> = emptyList(),
    val stats: Map<String, Int> = emptyMap(),
    val wishStats: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    val error: String? = null
)

class AdminViewModel : ViewModel() {
    private val api = ApiClient.getService()

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    fun setTab(tab: Int) { _state.update { it.copy(activeTab = tab) } }

    fun loadAll(adminId: Long) {
        loadStats(adminId)
        loadPendingMeteors(adminId)
        loadPendingWishes(adminId)
    }

    fun loadStats(adminId: Long) {
        viewModelScope.launch {
            try {
                val resp = api.getAdminStats(adminId)
                if (resp.isSuccess) _state.update { it.copy(stats = resp.data?.mapValues { v -> v.value } ?: emptyMap()) }
                val wResp = api.getWishStats(adminId)
                if (wResp.isSuccess) _state.update { it.copy(wishStats = wResp.data?.mapValues { v -> v.value } ?: emptyMap()) }
            } catch (_: Exception) {}
        }
    }

    fun loadPendingMeteors(adminId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val resp = api.getPendingReviews(adminId)
                if (resp.isSuccess) _state.update { it.copy(pendingMeteors = resp.data ?: emptyList(), isLoading = false) }
            } catch (_: Exception) { _state.update { it.copy(isLoading = false) } }
        }
    }

    fun loadPendingWishes(adminId: Long) {
        viewModelScope.launch {
            try {
                val resp = api.getPendingWishes(adminId)
                if (resp.isSuccess) _state.update { it.copy(pendingWishes = resp.data ?: emptyList()) }
            } catch (_: Exception) {}
        }
    }

    fun loadAllMeteors(adminId: Long, status: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val resp = api.getAllMessages(adminId, status)
                if (resp.isSuccess) _state.update { it.copy(allMeteors = resp.data ?: emptyList(), isLoading = false) }
            } catch (_: Exception) { _state.update { it.copy(isLoading = false) } }
        }
    }

    fun loadAllWishes(adminId: Long, status: String? = null) {
        viewModelScope.launch {
            try {
                val resp = api.getAllWishes(adminId, status)
                if (resp.isSuccess) _state.update { it.copy(allWishes = resp.data ?: emptyList()) }
            } catch (_: Exception) {}
        }
    }

    fun loadUsers(adminId: Long) {
        viewModelScope.launch {
            try {
                val resp = api.getAdminUsers(adminId)
                if (resp.isSuccess) _state.update { it.copy(users = resp.data ?: emptyList()) }
            } catch (_: Exception) {}
        }
    }

    fun reviewMeteor(messageId: Long, adminId: Long, status: String, reason: String = "") {
        viewModelScope.launch {
            try {
                api.reviewMessage(messageId, adminId, mapOf("status" to status, "reason" to reason))
                _state.update { it.copy(toastMessage = if (status == "approved") "已通过" else "已拒绝") }
                loadPendingMeteors(adminId)
                loadStats(adminId)
            } catch (e: Exception) { _state.update { it.copy(error = "操作失败") } }
        }
    }

    fun reviewWish(wishId: Long, adminId: Long, status: String, reason: String = "") {
        viewModelScope.launch {
            try {
                api.reviewWish(wishId, adminId, mapOf("status" to status, "reason" to reason))
                _state.update { it.copy(toastMessage = if (status == "approved") "已通过" else "已拒绝") }
                loadPendingWishes(adminId)
                loadStats(adminId)
            } catch (e: Exception) { _state.update { it.copy(error = "操作失败") } }
        }
    }

    fun deleteMeteor(messageId: Long, adminId: Long) {
        viewModelScope.launch {
            try {
                api.deleteMeteorAdmin(messageId, adminId)
                _state.update { it.copy(toastMessage = "已删除") }
                loadAllMeteors(adminId)
                loadPendingMeteors(adminId)
                loadStats(adminId)
            } catch (_: Exception) { _state.update { it.copy(error = "删除失败") } }
        }
    }

    fun deleteWish(wishId: Long, adminId: Long) {
        viewModelScope.launch {
            try {
                api.deleteWishAdmin(wishId, adminId)
                _state.update { it.copy(toastMessage = "已删除") }
                loadAllWishes(adminId)
                loadPendingWishes(adminId)
                loadStats(adminId)
            } catch (_: Exception) { _state.update { it.copy(error = "删除失败") } }
        }
    }

    fun deleteUser(userId: Long, adminId: Long) {
        viewModelScope.launch {
            try {
                api.deleteUserAdmin(userId, adminId)
                _state.update { it.copy(toastMessage = "用户已删除") }
                loadUsers(adminId)
            } catch (_: Exception) { _state.update { it.copy(error = "删除失败") } }
        }
    }

    fun clearToast() { _state.update { it.copy(toastMessage = null) } }
    fun clearError() { _state.update { it.copy(error = null) } }
}
