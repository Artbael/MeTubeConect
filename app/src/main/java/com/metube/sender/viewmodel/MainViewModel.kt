package com.metube.sender.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.metube.sender.data.DownloadOptions
import com.metube.sender.data.MeTubeApiFactory
import com.metube.sender.data.MeTubeRequest
import com.metube.sender.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Todos los estados posibles que puede mostrar la (mínima) UI de la app. */
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    // Regla B: hay que mostrar el modal de configuración antes de enviar
    data class ShowOptions(val url: String, val initialOptions: DownloadOptions) : UiState()
    // Envío terminado con éxito -> la Activity debe cerrarse
    data class Success(val message: String) : UiState()
    // Error de validación o de red -> la app se queda abierta para reintentar
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Regla A: TikTok / Instagram -> siempre "best" + formato de video, sin preguntar nada. */
    fun sendInstant(url: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            sendToServer(url = url, quality = "best", format = "mp4")
        }
    }

    /** Regla B: pide primero las opciones al usuario (con los valores por defecto guardados). */
    fun requestOptionsFor(url: String) {
        viewModelScope.launch {
            val defaults = settingsRepository.getDefaultOptions()
            _uiState.value = UiState.ShowOptions(url = url, initialOptions = defaults)
        }
    }

    /** Se llama al presionar "Enviar a MeTube" en el modal de la Regla B. */
    fun confirmOptionsAndSend(url: String, options: DownloadOptions, rememberAsDefault: Boolean) {
        viewModelScope.launch {
            if (rememberAsDefault) {
                settingsRepository.setDefaultOptions(options)
            }
            _uiState.value = UiState.Loading
            val (quality, format) = options.toMeTubeFields()
            sendToServer(url = url, quality = quality, format = format)
        }
    }

    fun resetToError(message: String) {
        _uiState.value = UiState.Error(message)
    }

    private suspend fun sendToServer(url: String, quality: String, format: String) {
        val serverUrl = settingsRepository.getServerUrl()

        if (serverUrl.isBlank()) {
            _uiState.value = UiState.Error(
                "No configuraste la dirección del servidor MeTube. Ve a Ajustes primero."
            )
            return
        }

        try {
            val api = MeTubeApiFactory.create(serverUrl)
            val response = api.addDownload(
                MeTubeRequest(url = url, quality = quality, format = format, folder = "")
            )

            if (response.isSuccessful) {
                _uiState.value = UiState.Success("Enviado a MeTube correctamente")
            } else {
                _uiState.value = UiState.Error(
                    "El servidor respondió con error (${response.code()}). Verifica la IP/puerto."
                )
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(
                "No se pudo conectar con el servidor MeTube: ${e.message ?: "error desconocido"}"
            )
        }
    }
}
