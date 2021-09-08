package com.linkedplanet.kotlininsightwrapper.atlas

import com.atlassian.applinks.api.ApplicationLink
import com.atlassian.applinks.api.ApplicationLinkResponseHandler
import com.atlassian.sal.api.net.Request
import com.atlassian.sal.api.net.Response
import com.atlassian.sal.api.net.ResponseException
import com.linkedplanet.kotlininsightwrapper.core.BaseHttpClient
import org.apache.http.HttpHeaders

class AtlasHttpClient(private val appLink: ApplicationLink) : BaseHttpClient() {

    override suspend fun executeRestCall(
        method: String,
        path: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?,
        headers: Map<String, String>
    ): String {
        try {
            //val pathWithParams = if(params != null) "$path?${URLEncoder.encode(params)}" else path
            val atlasMethod = Request.MethodType.valueOf(method)
            val parameters = encodeParams(params)
            val pathWithParams = if (params.isNotEmpty()) "$path?${parameters}" else path

            val requestFactory = appLink.createAuthenticatedRequestFactory()
            val requestWithoutBody = requestFactory.createRequest(atlasMethod, pathWithParams)
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

    override suspend fun executeDownload(
        method: String,
        url: String,
        params: Map<String, String>,
        body: String?
    ): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun executeUpload(
        method: String,
        url: String,
        params: Map<String, String>,
        mimeType: String,
        filename: String,
        byteArray: ByteArray
    ): ByteArray {
        TODO("Not yet implemented")
    }

}