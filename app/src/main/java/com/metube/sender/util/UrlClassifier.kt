package com.metube.sender.util

import android.util.Patterns

/**
 * Resultado de clasificar una URL entrante según las reglas del negocio.
 */
sealed class UrlRule {
    data class InstantSend(val url: String) : UrlRule()   // Regla A: TikTok / Instagram
    data class NeedsOptions(val url: String) : UrlRule()  // Regla B: YouTube y otros
    object Invalid : UrlRule()
}

object UrlClassifier {

    // Dominios que disparan el envío inmediato en máxima calidad
    private val instantDomains = listOf(
        "tiktok.com",
        "instagram.com",
        "instagr.am"
    )

    /**
     * Extrae la primera URL http/https encontrada dentro de un texto libre.
     * Esto cubre tanto el contenido del portapapeles como el "extra text"
     * que a veces viene acompañado de texto adicional al compartir
     * (por ejemplo, TikTok agrega frases antes del link).
     */
    fun extractUrl(rawText: String?): String? {
        if (rawText.isNullOrBlank()) return null
        val matcher = Patterns.WEB_URL.matcher(rawText)
        return if (matcher.find()) {
            rawText.substring(matcher.start(), matcher.end())
        } else {
            null
        }
    }

    fun isValidUrl(text: String): Boolean {
        return (text.startsWith("http://") || text.startsWith("https://")) &&
            Patterns.WEB_URL.matcher(text).matches()
    }

    /**
     * Clasifica el texto recibido y determina qué regla del flujo aplica.
     */
    fun classify(rawText: String?): UrlRule {
        val url = extractUrl(rawText) ?: return UrlRule.Invalid
        if (!isValidUrl(url)) return UrlRule.Invalid

        val host = try {
            java.net.URI(url).host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }

        val isInstant = instantDomains.any { domain -> host.contains(domain) }

        return if (isInstant) UrlRule.InstantSend(url) else UrlRule.NeedsOptions(url)
    }
}
