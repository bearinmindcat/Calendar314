package com.yourname.challengetracker.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputPath: String? = null
    private var isRecording = false

    fun startRecording(outputPath: String): Boolean {
        return try {
            // Stop any existing recording
            stopRecording()

            // Create MediaRecorder based on API level
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)  // Note: MP3 encoding not directly supported
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputPath)

                prepare()
                start()
            }

            currentOutputPath = outputPath
            isRecording = true
            Log.d("AudioRecorder", "Recording started: $outputPath")
            true

        } catch (e: IOException) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            false
        } catch (e: IllegalStateException) {
            Log.e("AudioRecorder", "Illegal state when starting recording", e)
            false
        } catch (e: SecurityException) {
            Log.e("AudioRecorder", "Missing RECORD_AUDIO permission", e)
            false
        }
    }

    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                if (isRecording) {
                    stop()
                    release()
                }
            }
            mediaRecorder = null
            isRecording = false

            val recordedPath = currentOutputPath
            currentOutputPath = null

            Log.d("AudioRecorder", "Recording stopped: $recordedPath")
            recordedPath

        } catch (e: RuntimeException) {
            Log.e("AudioRecorder", "Failed to stop recording properly", e)
            // Try to clean up even if stop failed
            try {
                mediaRecorder?.release()
            } catch (ignored: Exception) {}

            mediaRecorder = null
            isRecording = false

            // Delete potentially corrupted file
            currentOutputPath?.let { path ->
                File(path).delete()
            }
            currentOutputPath = null
            null
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                if (isRecording) {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error canceling recording", e)
        } finally {
            mediaRecorder = null
            isRecording = false

            // Delete the cancelled recording file
            currentOutputPath?.let { path ->
                File(path).delete()
            }
            currentOutputPath = null
        }
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    fun release() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error releasing recorder", e)
        }
        mediaRecorder = null
        isRecording = false
        currentOutputPath = null
    }
}
