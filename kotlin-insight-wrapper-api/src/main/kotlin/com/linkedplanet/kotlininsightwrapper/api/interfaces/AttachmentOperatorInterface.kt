package com.linkedplanet.kotlininsightwrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.model.InsightAttachment
import com.linkedplanet.kotlinhttpclient.error.DomainError

interface AttachmentOperatorInterface {

    suspend fun getAttachments(objectId: Int): Either<DomainError, List<InsightAttachment>>

    suspend fun downloadAttachment(url: String): Either<DomainError, ByteArray?>

    suspend fun uploadAttachment(
        objectId: Int,
        filename: String,
        byteArray: ByteArray,
        comment: String = ""
    ): Either<DomainError, List<InsightAttachment>>

    suspend fun deleteAttachment(attachmentId: Int): Either<DomainError, String>
}