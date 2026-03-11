package com.yourname.challengetracker.MainScreens.journal

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.yourname.challengetracker.audio.AudioRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryScreen(
    date: String,
    initialTitle: String = "",
    initialContent: String = "",
    initialAudioPath: String? = null,
    isEditable: Boolean = true,
    onBack: () -> Unit,
    onSave: (String, String, String?) -> Unit = { _, _, _ -> },
    onDelete: () -> Unit = {}
) {
    var entryTitle by remember { mutableStateOf(initialTitle) }
    var entryContent by remember { mutableStateOf(initialContent) }
    var audioPath by remember { mutableStateOf(initialAudioPath) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioRecorder = remember { AudioRecorder(context) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun startRecording() {
        scope.launch {
            errorMessage = null

            val audioDir = File(context.filesDir, "audio")
            if (!audioDir.exists()) audioDir.mkdirs()

            val timestamp = System.currentTimeMillis()
            val audioFile = File(audioDir, "journal_audio_${date}_$timestamp.m4a")

            if (audioRecorder.startRecording(audioFile.absolutePath)) {
                audioPath = audioFile.absolutePath
                isRecording = true
            } else {
                errorMessage = "Failed to start recording"
            }
        }
    }

    fun stopRecording() {
        scope.launch {
            isRecording = false
            val recordedPath = audioRecorder.stopRecording()

            if (recordedPath != null) {
                audioPath = recordedPath
                if (entryContent.isEmpty()) {
                    entryContent = "[Voice recording - ${recordingDuration} seconds]"
                }
                if (entryTitle.isEmpty()) {
                    entryTitle = "Voice Note - $date"
                }
            } else {
                errorMessage = "Recording failed"
                audioPath = null
            }
        }
    }

    fun playAudio() {
        audioPath?.let { path ->
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    start()
                    setOnCompletionListener {
                        isPlaying = false
                    }
                }
                isPlaying = true
            } catch (e: Exception) {
                errorMessage = "Cannot play audio: ${e.message}"
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying()) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            errorMessage = "Microphone permission is required"
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            while (isRecording) {
                delay(1000)
                recordingDuration++
                if (recordingDuration >= 180) {
                    stopRecording()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.release()
            mediaPlayer?.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal Entry - $date") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (initialAudioPath != null || initialContent.isNotEmpty()) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                    IconButton(onClick = { onSave(entryTitle, entryContent, audioPath) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = entryTitle,
                onValueChange = { if (isEditable) entryTitle = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = isEditable && !isRecording
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRecording) {
                        Text(
                            "Recording... ${recordingDuration}s / 180s",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { stopRecording() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop")
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                        == PackageManager.PERMISSION_GRANTED) {
                                        startRecording()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                enabled = !isPlaying
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Record")
                            }

                            if (audioPath != null) {
                                Button(onClick = { if (isPlaying) stopAudio() else playAudio() }) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Stop" else "Play"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isPlaying) "Stop" else "Play")
                                }
                            }
                        }
                    }

                    if (audioPath != null && !isRecording) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("✓ Voice recording attached", fontSize = 12.sp, color = Color.Green)
                    }
                }
            }

            OutlinedTextField(
                value = entryContent,
                onValueChange = { if (isEditable) entryContent = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                placeholder = { Text("Type your notes or record audio...") },
                enabled = isEditable && !isRecording
            )

            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = it, color = Color.Red, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
