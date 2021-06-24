package com.linkedplanet.kotlininsightwrapper.ktor

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.net.URLConnection

object AttachmentOperator {

    suspend fun getAttachments(objectId: Int): List<InsightAttachment> {
        return InsightConfig.httpClient.get {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/attachments/object/${objectId}")
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun downloadAttachment(obj: InsightAttachment): ByteArray {
        val url = obj.url
        val result = InsightConfig.httpClient.get<ByteArray> {
            url(url)
        }
        return result
    }

    suspend fun uploadAttachment(
        objectId: Int,
        filename: String,
        byteArray: ByteArray,
        comment: String = ""
    ): List<InsightAttachment> {
        val mimeType = URLConnection.guessContentTypeFromName(filename)
        InsightConfig.httpClient.post<String> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/attachments/object/${objectId}")
            header("Connection", "keep-alive")
            header("Cache-Control", "no-cache")
            body = MultiPartFormDataContent(
                formData {
                    this.append(
                        "file",
                        byteArray,
                        Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=$filename")
                        })
                    this.append(FormPart("encodedComment", comment))
                }
            )
        }
        return getAttachments(objectId)
    }

    suspend fun deleteAttachment(attachmentId: Int): String {
        val result = InsightConfig.httpClient.delete<String> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/attachments/${attachmentId}")
        }
        return result
    }
}