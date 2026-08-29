package com.qingyuan.lslife.feature.chat

import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VoicePlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _playingUrl = MutableStateFlow<String?>(null)
    val playingUrl: StateFlow<String?> = _playingUrl.asStateFlow()

    fun play(url: String) {
        if (_playingUrl.value == url) {
            // If already playing this url, toggle stop
            stop()
            return
        }
        
        stop() // Stop any previous playback

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnCompletionListener {
                    _playingUrl.value = null
                    releasePlayer()
                }
                setOnErrorListener { _, _, _ ->
                    _playingUrl.value = null
                    releasePlayer()
                    true
                }
                prepareAsync()
                setOnPreparedListener {
                    it.start()
                    _playingUrl.value = url
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _playingUrl.value = null
            releasePlayer()
        }
    }

    fun stop() {
        if (mediaPlayer?.isPlaying == true) {
            try { mediaPlayer?.stop() } catch (e: Exception) {}
        }
        _playingUrl.value = null
        releasePlayer()
    }

    private fun releasePlayer() {
        try { mediaPlayer?.release() } catch (e: Exception) {}
        mediaPlayer = null
    }
}
