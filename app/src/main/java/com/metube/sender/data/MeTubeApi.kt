package com.metube.sender.data

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface MeTubeApi {
    @POST("add")
    suspend fun addDownload(@Body request: MeTubeRequest): Response<MeTubeResponse>
}

/**
 * Construye un cliente Retrofit apuntando a la URL base configurada por el
 * usuario en Ajustes. Se crea "al vuelo" en cada envío porque el servidor
 * puede cambiar sin reinstalar la app.
 */
object MeTubeApiFactory {

    fun create(baseUrl: String): MeTubeApi {
        val normalized = normalizeBaseUrl(baseUrl)

        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeTubeApi::class.java)
    }

    /** Asegura esquema http(s):// y una sola barra final, como exige Retrofit. */
    private fun normalizeBaseUrl(input: String): String {
        var url = input.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        if (!url.endsWith("/")) {
            url = "$url/"
        }
        return url
    }
}
