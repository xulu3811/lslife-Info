package com.qingyuan.lslife.feature.chat

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

class AudioManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    
    private var currentRecordFile: File? = null
    private var recordStartTime = 0L

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingUrl = MutableStateFlow<String?>(null)
    val currentPlayingUrl: StateFlow<String?> = _currentPlayingUrl.asStateFlow()

    fun startRecording(): Boolean {
        try {
            val file = File(context.cacheDir, "audio_${UUID.randomUUID()}.m4a")
            currentRecordFile = file
            
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recordStartTime = System.currentTimeMillis()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording(cancel = true)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "录音引擎失败: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
            return false
        }
    }

    /**
     * @return Pair<FilePath, DurationSeconds>, or null if cancelled or too short
     */
    fun stopRecording(cancel: Boolean = false): Pair<String, Int>? {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
        }

        val duration = ((System.currentTimeMillis() - recordStartTime) / 1000).toInt()
        val file = currentRecordFile

        if (cancel || duration < 1 || file == null || !file.exists()) {
            file?.delete()
            return null
        }
        return Pair(file.absolutePath, duration)
    }

    fun playAudio(url: String) {
        val secureUrl = url.replace("http://", "https://")
        if (_currentPlayingUrl.value == secureUrl && _isPlaying.value) {
            stopAudio()
            return
        }
        
        stopAudio()
        
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(secureUrl)
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPlayingUrl.value = null
                    it.release()
                    player = null
                }
                prepareAsync()
                setOnPreparedListener {
                    it.start()
                    _isPlaying.value = true
                    _currentPlayingUrl.value = secureUrl
                }
                setOnErrorListener { _, what, extra ->
                    _isPlaying.value = false
                    _currentPlayingUrl.value = null
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        android.widget.Toast.makeText(context, "播放异常 (Error: $what, Extra: $extra)", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
            _currentPlayingUrl.value = null
        }
    }

    fun stopAudio() {
        try {
            player?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            player = null
            _isPlaying.value = false
            _currentPlayingUrl.value = null
        }
    }

    fun release() {
        stopRecording(cancel = true)
        stopAudio()
    }
}
