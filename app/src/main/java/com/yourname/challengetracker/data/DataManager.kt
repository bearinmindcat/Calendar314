package com.yourname.challengetracker.data

import android.content.Context
import com.yourname.challengetracker.data.model.AppData
import com.yourname.challengetracker.data.model.JournalEntryData
import com.yourname.challengetracker.data.model.ColoredDayData
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class DataManager(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val dataFile = File(context.filesDir, "app_data.json")
    private val backupFile = File(context.filesDir, "app_data_backup.json")
    private val audioDir = File(context.filesDir, "audio").apply {
        if (!exists()) mkdirs()
    }

    fun saveData(appData: AppData) {
        try {
            // Create backup of existing data first
            if (dataFile.exists()) {
                dataFile.copyTo(backupFile, overwrite = true)
            }

            // Write new data
            val jsonString = json.encodeToString(appData)
            dataFile.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            // If save fails, restore from backup
            if (backupFile.exists()) {
                backupFile.copyTo(dataFile, overwrite = true)
            }
        }
    }

    fun loadData(): AppData {
        return try {
            if (dataFile.exists()) {
                val jsonString = dataFile.readText()
                json.decodeFromString<AppData>(jsonString)
            } else {
                AppData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If load fails, try backup
            try {
                if (backupFile.exists()) {
                    val jsonString = backupFile.readText()
                    json.decodeFromString<AppData>(jsonString)
                } else {
                    AppData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AppData()
            }
        }
    }

    fun deleteAudioFile(audioPath: String?) {
        if (audioPath != null) {
            try {
                val audioFile = File(audioPath)
                if (audioFile.exists()) {
                    audioFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAudioFilePath(date: String): String {
        val timestamp = System.currentTimeMillis()
        return File(audioDir, "dream_audio_${date}_$timestamp.mp3").absolutePath
    }

    fun exportData(): String? {
        return try {
            if (dataFile.exists()) {
                dataFile.readText()
            } else {
                json.encodeToString(AppData())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importData(jsonString: String): Boolean {
        return try {
            // Validate JSON by parsing it
            val appData = json.decodeFromString<AppData>(jsonString)

            // Create backup before import
            if (dataFile.exists()) {
                dataFile.copyTo(backupFile, overwrite = true)
            }

            // Save imported data
            saveData(appData)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearAllData() {
        try {
            // Delete all audio files
            audioDir.listFiles()?.forEach { it.delete() }

            if (dataFile.exists()) {
                // Create backup before clearing
                dataFile.copyTo(backupFile, overwrite = true)
                dataFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}