package com.darekbx.sambaclient.ui.statistics

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RemoteStatistics(private val port: Int) {

    companion object {
        private const val STATISTICS_ENDPOINT = "/statistics"
    }

    suspend fun retrieveStatistics(
        maintenanceServerAddress: String,
        md5Credentials: String,
        subDir: String
    ): SubDirStatistics {
        return suspendCoroutine { continuation ->
            val query = "?path=$subDir"
            val url = "http://$maintenanceServerAddress:$port$STATISTICS_ENDPOINT$query"
            val statistics = downloadObject<SubDirStatistics>(url, md5Credentials)
            continuation.resume(statistics)
        }
    }

    suspend fun retrieveStatistics(
        maintenanceServerAddress: String,
        md5Credentials: String
    ): Statistics {
        return suspendCoroutine { continuation ->
            val url = "http://$maintenanceServerAddress:$port$STATISTICS_ENDPOINT"
            val statistics = downloadObject<Statistics>(url, md5Credentials)
            continuation.resume(statistics)
        }
    }

    private inline fun <reified T : Any> downloadObject(url: String, token: String): T {
        val httpClient = provideOkHttpClient()
        val request = buildGetRequest(url.toHttpUrl(), token)
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}")
        }
        val responseString = response.body?.string()
            ?: throw IOException("Response is empty")
        return gson.fromJson<T>(responseString, object : TypeToken<T>() {}.type)
    }

    private fun provideOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        )
        .build()

    private fun buildGetRequest(httpUrl: HttpUrl, token: String) = Request.Builder()
        .url(httpUrl)
        .method("GET", null)
        .header("Md5Authorization", token)
        .build()

    private val gson by lazy { Gson() }
}
