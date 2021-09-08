package com.linkedplanet.kotlininsightwrapper.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val GSON: Gson = GsonBuilder().create()

abstract class BaseHttpClient {

    abstract suspend fun executeRestCall(method: String, path: String, params: Map<String, String>, body: String?, contentType: String?, headers: Map<String, String> = emptyMap()): String

    abstract suspend fun executeDownload(method: String, url: String, params: Map<String, String>, body: String?): ByteArray

    abstract suspend fun executeUpload(method: String, url: String, params: Map<String, String>, mimeType: String, filename: String, byteArray: ByteArray): ByteArray

    suspend fun <T> executeRest(method: String, path: String, params: Map<String, String>, body: String?, contentType: String?, returnType: Type): T? =
        executeRestCall(method, path, params, body, contentType).let { GSON.fromJson<T>(it, returnType) }

    suspend fun <T> executeRestList(method: String, path: String, params: Map<String, String>, body: String?, contentType: String?, returnType: Type): List<T> =
        executeRestCall(method, path, params, body, contentType).let { GSON.fromJson(it, returnType) }

    suspend fun <T> executeGet(path: String, params: Map<String, String>, returnType: Type): T? =
        executeGetCall(path, params).let { GSON.fromJson<T>(it, returnType) }

    suspend fun <T> executeGetReturnList(path: String, params: Map<String, String>, returnType: Type): List<T>? =
        executeGetCall(path, params).let { GSON.fromJson(it, returnType) }

    suspend fun executeGetCall(path: String, params: Map<String, String>): String =
        executeRestCall("GET", path, params, null, null)

    fun encodeParams(map: Map<String, String>): String {
        return map.map { it.key + "=" + doEncoding(it.value) }.joinToString("&")
    }

    fun doEncoding(str: String): String =
        URLEncoder.encode(str, StandardCharsets.UTF_8.toString())
}