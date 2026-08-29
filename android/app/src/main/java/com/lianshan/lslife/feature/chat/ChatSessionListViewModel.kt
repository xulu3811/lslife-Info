package com.qingyuan.lslife.feature.chat

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.database.ImDao
import com.qingyuan.lslife.core.database.LocalConversationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.qingyuan.lslife.core.data.ImRepository

import com.qingyuan.lslife.core.data.TokenStore

data class ChatSessionListUiState(
    val loading: Boolean = false,
    val sessions: List<LocalConversationEntity> = emptyList(),
    val pinnedIds: Set<String> = emptySet(),
    val keepAlive: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChatSessionListViewModel @Inject constructor(
    private val imDao: ImDao,
    private val imRepository: ImRepository,
    private val tokenStore: TokenStore,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(ChatSessionListUiState())
    val state: StateFlow<ChatSessionListUiState> = _state
    
    private val prefs: SharedPreferences = context.getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)

    init {
        val initialPinned = prefs.getStringSet("pinned_sessions", emptySet())?.toSet() ?: emptySet()
        _state.update { it.copy(pinnedIds = initialPinned) }

        viewModelScope.launch {
            tokenStore.keepAliveFlow.collect { keepAlive ->
                _state.update { it.copy(keepAlive = keepAlive) }
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            imRepository.syncConversationsQuietly()
            imDao.getConversationsFlow().collect { localConvs ->
                _state.update { 
                    it.copy(
                        loading = false, 
                        sessions = sortSessions(localConvs, it.pinnedIds)
                    ) 
                }
            }
        }
    }

    fun toggleKeepAlive(enabled: Boolean) {
        viewModelScope.launch {
            tokenStore.setKeepAlive(enabled)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            imRepository.syncConversationsQuietly()
            _state.update { it.copy(loading = false) }
        }
    }

    fun togglePinSession(sessionId: String) {
        val currentPinned = _state.value.pinnedIds.toMutableSet()
        if (currentPinned.contains(sessionId)) {
            currentPinned.remove(sessionId)
        } else {
            currentPinned.add(sessionId)
        }
        prefs.edit().putStringSet("pinned_sessions", currentPinned).apply()
        _state.update { 
            it.copy(
                pinnedIds = currentPinned, 
                sessions = sortSessions(it.sessions, currentPinned)
            ) 
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            imRepository.deleteConversation(sessionId)
        }
    }

    private fun sortSessions(sessions: List<LocalConversationEntity>, pinnedIds: Set<String>): List<LocalConversationEntity> {
        return sessions.sortedWith { a, b ->
            val aPinned = pinnedIds.contains(a.conversationId)
            val bPinned = pinnedIds.contains(b.conversationId)
            if (aPinned && !bPinned) -1
            else if (!aPinned && bPinned) 1
            else b.lastMessageAt.compareTo(a.lastMessageAt)
        }
    }
}
