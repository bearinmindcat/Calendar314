package com.yourname.challengetracker.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class SpeechTranscriber(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _transcriptionState = MutableStateFlow(TranscriptionState())
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState

    data class TranscriptionState(
        val isListening: Boolean = false,
        val partialResult: String = "",
        val finalResult: String = "",
        val error: String? = null
    )

    fun startListening(
        onPartialResult: (String) -> Unit = {},
        onFinalResult: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device")
            return
        }

        cleanup() // Clean any existing recognizer

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("SpeechTranscriber", "Ready for speech")
                    _transcriptionState.value = _transcriptionState.value.copy(
                        isListening = true,
                        error = null
                    )
                }

                override fun onBeginningOfSpeech() {
                    Log.d("SpeechTranscriber", "Speech started")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Volume level - could be used for UI feedback
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    Log.d("SpeechTranscriber", "Speech ended")
                    _transcriptionState.value = _transcriptionState.value.copy(
                        isListening = false
                    )
                }

                override fun onError(error: Int) {
                    val errorMessage = getErrorMessage(error)
                    Log.e("SpeechTranscriber", "Recognition error: $errorMessage")

                    _transcriptionState.value = _transcriptionState.value.copy(
                        isListening = false,
                        error = errorMessage
                    )

                    onError(errorMessage)

                    // Don't cleanup on all errors - some are recoverable
                    if (error == SpeechRecognizer.ERROR_CLIENT ||
                        error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                        cleanup()
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val transcribedText = matches[0]
                        Log.d("SpeechTranscriber", "Final result: $transcribedText")

                        _transcriptionState.value = _transcriptionState.value.copy(
                            finalResult = transcribedText,
                            partialResult = "",
                            isListening = false
                        )

                        onFinalResult(transcribedText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    if (!matches.isNullOrEmpty()) {
                        val partialText = matches[0]
                        Log.d("SpeechTranscriber", "Partial result: $partialText")

                        _transcriptionState.value = _transcriptionState.value.copy(
                            partialResult = partialText
                        )

                        onPartialResult(partialText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

                // For continuous listening (up to ~60 seconds)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            }

            startListening(intent)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _transcriptionState.value = _transcriptionState.value.copy(
            isListening = false
        )
    }

    fun cleanup() {
        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        speechRecognizer = null
        _transcriptionState.value = TranscriptionState()
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            else -> "Unknown error"
        }
    }

    // Legacy method for compatibility - redirects to startListening
    fun transcribeAudio(audioPath: String, onResult: (String?) -> Unit) {
        // Note: SpeechRecognizer cannot transcribe files, only live audio
        // This method is kept for compatibility but will use live listening instead
        Log.w("SpeechTranscriber", "File transcription not supported. Using live listening instead.")

        startListening(
            onFinalResult = { result ->
                onResult(result)
            },
            onError = { error ->
                Log.e("SpeechTranscriber", "Transcription error: $error")
                onResult(null)
            }
        )
    }
}
