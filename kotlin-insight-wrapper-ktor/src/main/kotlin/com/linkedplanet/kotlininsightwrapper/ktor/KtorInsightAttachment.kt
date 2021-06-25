package com.linkedplanet.kotlininsightwrapper.ktor

import com.linkedplanet.kotlininsightwrapper.core.InsightAttachment

suspend fun InsightAttachment.getBytes(): ByteArray {
    return AttachmentOperator.downloadAttachment(this)
}

suspend fun InsightAttachment.delete(): Boolean {
    if (id <= 0) {
        return false
    }
    AttachmentOperator.deleteAttachment(this.id)
    return true
}