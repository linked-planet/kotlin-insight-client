package com.linkedplanet.kotlininsightwrapper.atlas

import com.atlassian.applinks.api.ApplicationLink
import com.atlassian.applinks.api.ApplicationLinkResponseHandler
import com.atlassian.sal.api.net.Request
import com.atlassian.sal.api.net.Response
import com.atlassian.sal.api.net.ResponseException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.apache.http.HttpHeaders
import java.lang.reflect.Type
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val GSON: Gson = GsonBuilder().create()


fun <T> executeRest(appLink: ApplicationLink, method: Request.MethodType, path: String, params: Map<String, String>, body: String?, contentType: String?, returnType: Type): T? =
    executeRestCall(appLink, method, path, params, body, contentType).let { GSON.fromJson<T>(it, returnType) }

fun <T> executeRestList(appLink: ApplicationLink, method: Request.MethodType, path: String, params: Map<String, String>, body: String?, contentType: String?, returnType: Type): List<T> =
    executeRestCall(appLink, method, path, params, body, contentType).let { GSON.fromJson(it, returnType) }

fun <T> executeGet(appLink: ApplicationLink, path: String, params: Map<String, String>, returnType: Type): T? =
    executeGetCall(appLink, path, params).let { GSON.fromJson<T>(it, returnType) }

fun <T> executeGetReturnList(appLink: ApplicationLink, path: String, params: Map<String, String>, returnType: Type): List<T>? =
    executeGetCall(appLink, path, params).let { GSON.fromJson(it, returnType) }

fun executeGetCall(appLink: ApplicationLink, path: String, params: Map<String, String>): String =
    executeRestCall(appLink, Request.MethodType.GET, path, params, null, null)

fun executeRestCall(appLink: ApplicationLink, method: Request.MethodType, path: String, params: Map<String, String>, body: String?, contentType: String?): String {
    try {
        //val pathWithParams = if(params != null) "$path?${URLEncoder.encode(params)}" else path
        val parameters = encodeParams(params)
        val pathWithParams = if(params.isNotEmpty()) "$path?${parameters}" else path

        val requestFactory = appLink.createAuthenticatedRequestFactory()
        val requestWithoutBody = requestFactory.createRequest(method, pathWithParams)
        val request = if (body == null) {
            requestWithoutBody
        } else {
            requestWithoutBody.setRequestBody(body).setHeader(HttpHeaders.CONTENT_TYPE, contentType)
        }
        return request.execute(object : ApplicationLinkResponseHandler<String> {
            override fun credentialsRequired(response: Response): String? {
                return null
            }

            @Throws(ResponseException::class)
            override fun handle(response: Response): String? {
                return when {
                    response.isSuccessful -> response.responseBodyAsString
                    else -> {
                        val statusCode = response.statusCode
                        val content = response.statusText
                        val errorWithStatusCode = "Call to " + path + " failed - " + response.statusCode
                        throw ResponseException(errorWithStatusCode)
                    }
                }
            }
        })
    } catch (e: ResponseException) {
        throw Exception("Jira/Insight hat ein internes Problem festgestellt")
    }
}

fun encodeParams(map: Map<String, String>): String {
    return map.map { it.key + "=" + doEncoding(it.value) }.joinToString("&")
}

fun doEncoding(str: String): String =
    URLEncoder.encode(str, StandardCharsets.UTF_8.toString())