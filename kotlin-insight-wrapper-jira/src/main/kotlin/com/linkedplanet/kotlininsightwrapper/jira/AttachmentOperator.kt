package com.linkedplanet.kotlininsightwrapper.jira

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.InsightAttachment
import com.linkedplanet.kotlininsightwrapper.api.interfaces.AttachmentOperatorInterface
import com.linkedplanet.kotlininsightwrapper.core.DomainError

object AttachmentOperator : AttachmentOperatorInterface {

    override suspend fun getAttachments(objectId: Int): Either<DomainError, List<InsightAttachment>> {
        TODO("Not yet implemented")
    }

    override suspend fun downloadAttachment(url: String): Either<DomainError, ByteArray> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadAttachment(
        objectId: Int,
        filename: String,
        byteArray: ByteArray,
        comment: String
    ): Either<DomainError, List<InsightAttachment>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAttachment(attachmentId: Int): Either<DomainError, String> {
        TODO("Not yet implemented")
    }

}