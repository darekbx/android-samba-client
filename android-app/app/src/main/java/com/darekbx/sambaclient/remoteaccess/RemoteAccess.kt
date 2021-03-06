package com.darekbx.sambaclient.remoteaccess

import android.util.Log
import com.darekbx.sambaclient.samba.SambaFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import okio.Okio
import okio.buffer
import okio.sink
import java.io.IOException
import java.io.OutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class RemoteAccess(
    private val port: Int
) {
    companion object {
        private const val AUTH_ENDPOINT = "/authenticate"
        private const val LIST_ENDPOINT = "/list"
        private const val FILE_DETAILS_ENDPOINT = "/file_details"
        private const val FILE_DELETE_ENDPOINT = "/file_delete"
        private const val FILE_DOWNLOAD_ENDPOINT = "/file_download"
    }

    private var serverAddress: String = ""
    private var md5Credentials: String = ""
    private var md5IPCredentials: String = ""

    fun setCredentials(serverAddress: String, md5Credentials: String, md5IPCredentials: String) {
        this.serverAddress = serverAddress
        this.md5Credentials = md5Credentials
        this.md5IPCredentials = md5IPCredentials
    }

    suspend fun authorize(): AuthorizeResult {
        return suspendCoroutine { continuation ->
            val url = "http://$serverAddress:$port$AUTH_ENDPOINT"
            val statistics = uploadObject<AuthorizeResult>(url, "{}")
            continuation.resume(statistics)
        }
    }

    suspend fun list(path: String): List<SambaFile> {
        return suspendCoroutine { continuation ->
            val query = "?path=$path"
            val url = "http://$serverAddress:$port$LIST_ENDPOINT$query"
            val list = downloadObject<List<SambaFile>>(url)
            continuation.resume(list)
        }
    }

    suspend fun fileDetails(path: String): SambaFile {
        return suspendCoroutine { continuation ->
            val query = "?path=$path"
            val url = "http://$serverAddress:$port$FILE_DETAILS_ENDPOINT$query"
            val sambaFile = downloadObject<SambaFile>(url)
            continuation.resume(sambaFile)
        }
    }

    suspend fun fileDelete(path: String): CommonResult {
        return suspendCoroutine { continuation ->
            val query = "?path=$path"
            val url = "http://$serverAddress:$port$FILE_DELETE_ENDPOINT$query"
            val result = downloadObject<CommonResult>(url)
            continuation.resume(result)
        }
    }

    suspend fun fileDownload(path: String, outputStream: OutputStream): String {
        return suspendCoroutine { continuation ->
            val query = "?path=$path"
            val url = "http://$serverAddress:$port$FILE_DOWNLOAD_ENDPOINT$query"
            downloadFile(url, outputStream)
            continuation.resume(url)
        }
    }

    private fun downloadFile(url: String, outputStream: OutputStream) {
        val httpClient = provideOkHttpClient()
        val request = buildGetRequest(url.toHttpUrl())
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}")
        }
        outputStream.sink().buffer().use {
            it.writeAll(response.body!!.source())
        }
    }

    private inline fun <reified T : Any> downloadObject(url: String): T {
        val httpClient = provideOkHttpClient()
        val request = buildGetRequest(url.toHttpUrl())
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}")
        }
        val responseString = response.body?.string()
            ?: throw IOException("Response is empty")
        return gson.fromJson<T>(responseString, object : TypeToken<T>() {}.type)
    }

    private inline fun <reified T : Any> uploadObject(url: String, data: String): T {
        val httpClient = provideOkHttpClient()
        val body = data?.toRequestBody("application/json".toMediaType())
        val request = buildPostRequest(url.toHttpUrl(), body)
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

    private fun buildGetRequest(httpUrl: HttpUrl) =
        Request.Builder()
            .url(httpUrl)
            .method("GET", null)
            .addAuthentication(md5Credentials, md5IPCredentials)
            .build()

    private fun buildPostRequest(httpUrl: HttpUrl, body: RequestBody) =
        Request.Builder()
            .url(httpUrl)
            .method("POST", body)
            .addAuthentication(md5Credentials, md5IPCredentials)
            .post(body)
            .build()

    private fun Request.Builder.addAuthentication(md5Credentials: String, md5IPToken: String) =
        header("Md5Authorization", md5Credentials)
            .header("Md5IPAuthorization", md5IPToken)

    private val gson by lazy { Gson() }
}
