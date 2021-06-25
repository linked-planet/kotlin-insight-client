package com.linkedplanet.kotlininsightwrapper.core

abstract class BaseAttachmentOperator(val baseUrl: String) {

    val attachmentsEndpoint: String = "rest/insight/1.0/attachments"
        get() = "$baseUrl/$field"

    val attachmentsObjectEndpoint: String = "rest/insight/1.0/attachments/object"
        get() = "$baseUrl/$field"

    abstract suspend fun getAttachments(
        objectId: Int
    ): List<InsightAttachment>

    abstract suspend fun downloadAttachment(obj: InsightAttachment): ByteArray

    abstract suspend fun uploadAttachment(
        objectId: Int,
        filename: String,
        byteArray: ByteArray,
        comment: String = ""
    ): List<InsightAttachment>

    abstract suspend fun deleteAttachment(attachmentId: Int): String
}