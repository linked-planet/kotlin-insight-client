package com.linkedplanet.kotlininsightwrapper.core

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlininsightwrapper.api.model.InsightAttachment
import com.linkedplanet.kotlininsightwrapper.api.error.DomainError
import com.linkedplanet.kotlininsightwrapper.api.http.InsightConfig
import com.linkedplanet.kotlininsightwrapper.api.interfaces.AttachmentOperatorInterface
import java.net.URLConnection

object AttachmentOperator: AttachmentOperatorInterface {

    override suspend fun getAttachments(objectId: Int): Either<DomainError, List<InsightAttachment>> = either {
        val result: Either<DomainError, List<InsightAttachment>> = InsightConfig.httpClient.executeRestList(
            "GET",
            "rest/insight/1.0/attachments/object/${objectId}",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<InsightAttachment>>() {}.type
        )
        result.bind()
    }

    // TODO: Downloads not working in both
    override suspend fun downloadAttachment(url: String): Either<DomainError, ByteArray?> = either {
        val result: Either<DomainError, ByteArray?> = InsightConfig.httpClient.executeDownload(
            "GET",
            url,
            emptyMap(),
            null,
            null
        )
        result.bind()
    }

    // TODO: Uploads not working in both
    override suspend fun uploadAttachment(
        objectId: Int,
        filename: String,
        byteArray: ByteArray,
        comment: String
    ): Either<DomainError, List<InsightAttachment>> = either {
        val mimeType = URLConnection.guessContentTypeFromName(filename)
        val result = InsightConfig.httpClient.executeUpload(
            "POST",
            "/rest/insight/1.0/attachments/object/${objectId}",
            emptyMap(),
            mimeType,
            filename,
            byteArray
        ).bind()
        getAttachments(objectId).bind()
    }

    override suspend fun deleteAttachment(attachmentId: Int): Either<DomainError, String> = either {
        val result = InsightConfig.httpClient.executeRestCall(
            "DELETE",
            "/rest/insight/1.0/attachments/${attachmentId}",
            emptyMap(),
            null,
            "application/json"
        ).bind()
        result
    }
}