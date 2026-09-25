package com.metube.sender

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metube.sender.data.SettingsRepository
import com.metube.sender.ui.DownloadOptionsScreen
import com.metube.sender.ui.MeTubeSenderTheme
import com.metube.sender.ui.SettingsScreen
import com.metube.sender.util.UrlClassifier
import com.metube.sender.util.UrlRule
import com.metube.sender.viewmodel.MainViewModel
import com.metube.sender.viewmodel.UiState
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)

        // Si viene desde "Compartir con...", procesa el Intent inmediatamente.
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val incomingText = intent.getStringExtra(Intent.EXTRA_TEXT)
            routeUrl(incomingText)
        }

        setContent {
            var settingsVisible by remember { mutableStateOf(false) }
            val uiState by viewModel.uiState.collectAsState()

            MeTubeSenderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (settingsVisible) {
                        SettingsScreen(
                            settingsRepository = settingsRepository,
                            onBack = { settingsVisible = false }
                        )
                    } else {
                        MainScreen(
                            uiState = uiState,
                            onOpenSettings = { settingsVisible = true },
                            onConfirmOptions = { url, options, remember ->
                                viewModel.confirmOptionsAndSend(url, options, remember)
                            },
                            onCancel = { finishAndRemoveTask() },
                            onRetryPaste = {
                                val pasted = readClipboardText()
                                routeUrl(pasted)
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Se dispara cuando la ventana obtiene el foco activo de Android.
     * Esto permite leer el portapapeles sin bloqueos de privacidad del OS al abrir la app.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && intent?.action != Intent.ACTION_SEND) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString() ?: ""
                    if (text.isNotBlank()) {
                        routeUrl(text)
                    }
                }
            }
        }
    }

    private fun readClipboardText(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        return if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(this)?.toString()
        } else {
            null
        }
    }

    /** Aplica la clasificación (Regla A / Regla B / inválida) y actualiza el estado. */
    private fun routeUrl(rawText: String?) {
        when (val rule = UrlClassifier.classify(rawText)) {
            is UrlRule.InstantSend -> viewModel.sendInstant(rule.url)
            is UrlRule.NeedsOptions -> viewModel.requestOptionsFor(rule.url)
            UrlRule.Invalid -> viewModel.resetToError(
                "No se encontró un enlace válido. Copia una URL o compártela desde la app de origen."
            )
        }
    }

    @Composable
    private fun MainScreen(
        uiState: UiState,
        onOpenSettings: () -> Unit,
        onConfirmOptions: (String, com.metube.sender.data.DownloadOptions, Boolean) -> Unit,
        onCancel: () -> Unit,
        onRetryPaste: () -> Unit
    ) {
        LaunchedEffect(uiState) {
            if (uiState is UiState.Success) {
                Toast.makeText(applicationContext, uiState.message, Toast.LENGTH_SHORT).show()
                delay(600)
                finishAndRemoveTask()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Botón de Ajustes en la esquina superior derecha
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
            }

            // Contenido principal de la pantalla
            when (uiState) {
                is UiState.ShowOptions -> DownloadOptionsScreen(
                    url = uiState.url,
                    initialOptions = uiState.initialOptions,
                    isSending = false,
                    onConfirm = { options, remember -> onConfirmOptions(uiState.url, options, remember) },
                    onCancel = onCancel
                )

                UiState.Loading -> LoadingContent()

                is UiState.Error -> ErrorContent(message = uiState.message, onRetryPaste = onRetryPaste, onClose = onCancel)

                UiState.Idle, is UiState.Success -> LoadingContent()
            }

            // Firma personalizada Artbael en la parte inferior
            Text(
                text = "Developed by Artbael",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }

    @Composable
    private fun LoadingContent() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Enviando a MeTube…")
        }
    }

    @Composable
    private fun ErrorContent(message: String, onRetryPaste: () -> Unit, onClose: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ups, algo no salió bien", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetryPaste, modifier = Modifier.fillMaxWidth()) {
                Text("Reintentar desde portapapeles")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
            }
        }
    }
}
