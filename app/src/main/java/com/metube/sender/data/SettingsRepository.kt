package com.metube.sender.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "metube_settings")

/**
 * Guarda de forma persistente:
 *  1) La dirección del servidor MeTube.
 *  2) Las opciones predeterminadas para el modal de la Regla B.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val DEFAULT_MODE = stringPreferencesKey("default_mode")
        val DEFAULT_VIDEO_QUALITY = stringPreferencesKey("default_video_quality")
        val DEFAULT_VIDEO_FORMAT = stringPreferencesKey("default_video_format")
        val DEFAULT_AUDIO_BITRATE = stringPreferencesKey("default_audio_bitrate")
        val DEFAULT_AUDIO_FORMAT = stringPreferencesKey("default_audio_format")
    }

    val serverUrlFlow: Flow<String> = context.dataStore.data.map { it[Keys.SERVER_URL] ?: "" }

    suspend fun getServerUrl(): String = serverUrlFlow.first()

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[Keys.SERVER_URL] = url.trim() }
    }

    suspend fun getDefaultOptions(): DownloadOptions {
        val prefs = context.dataStore.data.first()
        val mode = DownloadMode.entries.firstOrNull {
            it.name == prefs[Keys.DEFAULT_MODE]
        } ?: DownloadMode.VIDEO_AUDIO

        return DownloadOptions(
            mode = mode,
            videoQuality = prefs[Keys.DEFAULT_VIDEO_QUALITY] ?: "best",
            videoFormat = prefs[Keys.DEFAULT_VIDEO_FORMAT] ?: "mp4",
            audioBitrate = prefs[Keys.DEFAULT_AUDIO_BITRATE] ?: "320",
            audioFormat = prefs[Keys.DEFAULT_AUDIO_FORMAT] ?: "mp3"
        )
    }

    suspend fun setDefaultOptions(options: DownloadOptions) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_MODE] = options.mode.name
            prefs[Keys.DEFAULT_VIDEO_QUALITY] = options.videoQuality
            prefs[Keys.DEFAULT_VIDEO_FORMAT] = options.videoFormat
            prefs[Keys.DEFAULT_AUDIO_BITRATE] = options.audioBitrate
            prefs[Keys.DEFAULT_AUDIO_FORMAT] = options.audioFormat
        }
    }
}
