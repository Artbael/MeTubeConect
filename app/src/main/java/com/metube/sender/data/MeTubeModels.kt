package com.metube.sender.data

import com.google.gson.annotations.SerializedName

/**
 * Cuerpo JSON que espera el endpoint POST /add de MeTube.
 * https://github.com/alexta69/metube
 */
data class MeTubeRequest(
    @SerializedName("url") val url: String,
    @SerializedName("quality") val quality: String,   // "best", "1080", "720", "480", etc.
    @SerializedName("format") val format: String,     // "any", "mp4", "mkv", "mp3", "m4a", "aac"
    @SerializedName("folder") val folder: String = ""
)

data class MeTubeResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("msg") val msg: String?
)

/** Modo elegido por el usuario para la Regla B (YouTube / otros). */
enum class DownloadMode { VIDEO_AUDIO, AUDIO_ONLY }

/** Opciones completas construidas desde el modal de configuración rápida. */
data class DownloadOptions(
    val mode: DownloadMode = DownloadMode.VIDEO_AUDIO,
    val videoQuality: String = "best",     // best, 1080, 720, 480
    val videoFormat: String = "mp4",       // mp4, mkv
    val audioBitrate: String = "320",      // 320, 256, 128 (kbps)
    val audioFormat: String = "mp3"        // mp3, aac, m4a
) {
    /** Traduce las opciones de la UI al formato/calidad que espera MeTube. */
    fun toMeTubeFields(): Pair<String, String> {
        return if (mode == DownloadMode.AUDIO_ONLY) {
            audioBitrate to audioFormat
        } else {
            videoQuality to videoFormat
        }
    }
}
