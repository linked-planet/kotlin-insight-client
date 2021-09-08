package com.linkedplanet.kotlinInsightWrapper

import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlininsightwrapper.core.InsightAttachment
import com.linkedplanet.kotlininsightwrapper.core.InsightConfig
import java.net.URLConnection

object AttachmentOperator {

    suspend fun getAttachments(objectId: Int): List<InsightAttachment> {
        return InsightConfig.httpClient.executeRestList(
            "GET",
            "rest/insight/1.0/attachments/object/${objectId}",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<InsightAttachment>>() {}.type
        )
    }

    // TODO: Downloads not working in both
    suspend fun downloadAttachment(obj: InsightAttachment): ByteArray {
        val path = obj.url.replace("${InsightConfig.baseUrl}/", "")

        val result = InsightConfig.httpClient.executeGet<ByteArray>(
            path,
            emptyMap(),
            object: TypeToken<ByteArray>() {}.type
        )!!
        return result
    }

    // TODO: Uploads not working in both
    suspend fun uploadAttachment(
        objectId: Int,
        filename: String,
        byteArray: ByteArray,
        comment: String = ""
    ): List<InsightAttachment> {
        val mimeType = URLConnection.guessContentTypeFromName(filename)
        InsightConfig.httpClient.executeUpload(
            "POST",
            "${InsightConfig.baseUrl}/rest/insight/1.0/attachments/object/${objectId}",
            emptyMap(),
            mimeType,
            filename,
            byteArray
        )
        return getAttachments(objectId)
    }

    suspend fun deleteAttachment(attachmentId: Int): String {
        val result = InsightConfig.httpClient.executeRestCall(
            "DELETE",
            "/rest/insight/1.0/attachments/${attachmentId}",
            emptyMap(),
            null,
            "application/json"
        )
        return result
    }
}