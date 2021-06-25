package com.linkedplanet.kotlininsightwrapper.ktor

import com.linkedplanet.kotlininsightwrapper.core.BaseAttachmentOperator
import com.linkedplanet.kotlininsightwrapper.core.InsightAttachment
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.net.URLConnection

object KtorAttachmentOperator : BaseAttachmentOperator(InsightConfig.baseUrl) {

    override suspend fun getAttachments(objectId: Int): List<InsightAttachment> {
        return InsightConfig.httpClient.get {
            url("${attachmentsObjectEndpoint}/${objectId}")
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun downloadAttachment(obj: InsightAttachment): ByteArray {
        return InsightConfig.httpClient.get {
            url(obj.url)
        }
    }

    override suspend fun uploadAttachment(
        objectId: Int,
        filename: String,
        byteArray: ByteArray,
        comment: String
    ): List<InsightAttachment> {
        val mimeType = URLConnection.guessContentTypeFromName(filename)
        InsightConfig.httpClient.post<String> {
            url("${attachmentsObjectEndpoint}/${objectId}")
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

    override suspend fun deleteAttachment(attachmentId: Int): String {
        return InsightConfig.httpClient.delete {
            url("${attachmentsEndpoint}/${attachmentId}")
        }
    }
}