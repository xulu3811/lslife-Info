package com.qingyuan.lslife.feature.chat

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMillis: Long = 0

    fun startRecording(): File? {
        try {
            val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file
            
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            startTimeMillis = System.currentTimeMillis()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            release()
            return null
        }
    }

    /**
     * @return Pair of <File, Duration_Seconds>. If duration < 1s, returns null and deletes file.
     */
    fun stopRecording(): Pair<File, Int>? {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            
            val durationMillis = System.currentTimeMillis() - startTimeMillis
            val durationSeconds = (durationMillis / 1000).toInt()
            
            val file = currentOutputFile
            currentOutputFile = null
            
            if (file != null && file.exists()) {
                if (durationSeconds < 1) {
                    file.delete()
                    return null
                }
                return Pair(file, durationSeconds)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            release()
        }
        return null
    }

    fun cancelRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }

    fun getMaxAmplitude(): Int {
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun release() {
        try {
            recorder?.release()
        } catch (e: Exception) {}
        recorder = null
        currentOutputFile = null
    }
}
