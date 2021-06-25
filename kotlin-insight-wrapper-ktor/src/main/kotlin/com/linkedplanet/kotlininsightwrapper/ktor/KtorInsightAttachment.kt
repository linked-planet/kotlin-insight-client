package com.linkedplanet.kotlininsightwrapper.ktor

import com.linkedplanet.kotlininsightwrapper.core.InsightAttachment

suspend fun InsightAttachment.getBytes(): ByteArray {
    return KtorAttachmentOperator.downloadAttachment(this)
}

suspend fun InsightAttachment.delete(): Boolean {
    if (id <= 0) {
        return false
    }
    KtorAttachmentOperator.deleteAttachment(this.id)
    return true
}