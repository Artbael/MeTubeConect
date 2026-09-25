package com.metube.sender.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metube.sender.data.DownloadMode
import com.metube.sender.data.DownloadOptions

private val videoQualities = listOf("best", "1080", "720", "480")
private val videoFormats = listOf("mp4", "mkv")
private val audioBitrates = listOf("320", "256", "128")
private val audioFormats = listOf("mp3", "aac", "m4a")

/**
 * Modal rápido que se muestra para la Regla B (YouTube / otros dominios).
 * onConfirm entrega las opciones finales y si deben guardarse como
 * predeterminadas para la próxima vez.
 */
@Composable
fun DownloadOptionsScreen(
    url: String,
    initialOptions: DownloadOptions,
    isSending: Boolean,
    onConfirm: (DownloadOptions, rememberAsDefault: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var mode by remember { mutableStateOf(initialOptions.mode) }
    var videoQuality by remember { mutableStateOf(initialOptions.videoQuality) }
    var videoFormat by remember { mutableStateOf(initialOptions.videoFormat) }
    var audioBitrate by remember { mutableStateOf(initialOptions.audioBitrate) }
    var audioFormat by remember { mutableStateOf(initialOptions.audioFormat) }
    var rememberDefault by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Enviar a MeTube", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )

            Divider()

            Text("Modo", style = MaterialTheme.typography.titleMedium)
            SegmentedTwoOptions(
                optionALabel = "Video + Audio",
                optionBLabel = "Solo Audio",
                isOptionASelected = mode == DownloadMode.VIDEO_AUDIO,
                onSelectA = { mode = DownloadMode.VIDEO_AUDIO },
                onSelectB = { mode = DownloadMode.AUDIO_ONLY }
            )

            if (mode == DownloadMode.VIDEO_AUDIO) {
                Text("Calidad de video", style = MaterialTheme.typography.titleMedium)
                ChipRow(options = videoQualities, selected = videoQuality) { videoQuality = it }

                Text("Formato / Códec", style = MaterialTheme.typography.titleMedium)
                ChipRow(options = videoFormats, selected = videoFormat) { videoFormat = it }
            } else {
                Text("Bitrate de audio (kbps)", style = MaterialTheme.typography.titleMedium)
                ChipRow(options = audioBitrates, selected = audioBitrate) { audioBitrate = it }

                Text("Formato de audio", style = MaterialTheme.typography.titleMedium)
                ChipRow(options = audioFormats, selected = audioFormat) { audioFormat = it }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = rememberDefault, onCheckedChange = { rememberDefault = it })
                Spacer(Modifier.width(4.dp))
                Text("Guardar como opciones predeterminadas")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val options = DownloadOptions(
                        mode = mode,
                        videoQuality = videoQuality,
                        videoFormat = videoFormat,
                        audioBitrate = audioBitrate,
                        audioFormat = audioFormat
                    )
                    onConfirm(options, rememberDefault)
                },
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Enviar a MeTube", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
private fun SegmentedTwoOptions(
    optionALabel: String,
    optionBLabel: String,
    isOptionASelected: Boolean,
    onSelectA: () -> Unit,
    onSelectB: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = isOptionASelected, onClick = onSelectA, label = { Text(optionALabel) })
        FilterChip(selected = !isOptionASelected, onClick = onSelectB, label = { Text(optionBLabel) })
    }
}

@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option) }
            )
        }
    }
}
