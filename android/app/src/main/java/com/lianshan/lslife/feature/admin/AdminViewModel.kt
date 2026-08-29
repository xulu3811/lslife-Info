package com.qingyuan.lslife.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.model.*
import com.qingyuan.lslife.core.network.AdminApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val api: AdminApiService
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<AdminDashboardData?>(null)
    val dashboardState: StateFlow<AdminDashboardData?> = _dashboardState.asStateFlow()

    private val _kycUsers = MutableStateFlow<List<AdminKycUser>>(emptyList())
    val kycUsers: StateFlow<List<AdminKycUser>> = _kycUsers.asStateFlow()

    private val _posts = MutableStateFlow<List<AdminPost>>(emptyList())
    val posts: StateFlow<List<AdminPost>> = _posts.asStateFlow()

    private val _users = MutableStateFlow<List<AdminUser>>(emptyList())
    val users: StateFlow<List<AdminUser>> = _users.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                val res = api.getDashboard()
                _dashboardState.value = res.data
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadKycUsers() {
        viewModelScope.launch {
            try {
                val res = api.getKycUsers()
                _kycUsers.value = res.data ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun auditKycUser(id: String, approve: Boolean, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                api.auditKycUser(id, AdminActionRequest(if (approve) "approve" else "reject"))
                loadKycUsers()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun loadPosts(status: String? = null) {
        viewModelScope.launch {
            try {
                val res = api.getPosts(status)
                _posts.value = res.data ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun auditPost(id: String, action: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                api.auditPost(id, AdminActionRequest(action))
                loadPosts("pending_review")
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun loadUsers(keyword: String = "") {
        viewModelScope.launch {
            try {
                val res = api.getUsers(keyword = keyword)
                _users.value = res.data?.items ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun banUser(id: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                api.updateUserStatus(id, AdminStatusRequest("banned"))
                loadUsers()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
 
