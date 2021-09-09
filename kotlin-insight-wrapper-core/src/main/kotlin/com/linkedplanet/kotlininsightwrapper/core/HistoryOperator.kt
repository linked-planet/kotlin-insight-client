package com.linkedplanet.kotlininsightwrapper.core

import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlininsightwrapper.core.InsightConfig
import com.linkedplanet.kotlininsightwrapper.core.InsightHistoryItem

object HistoryOperator {

    suspend fun getHistory(objectId: Int): List<InsightHistoryItem> {
        return InsightConfig.httpClient.executeRestList<InsightHistoryItem>(
            "GET",
            "rest/insight/1.0/object/${objectId}/history",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<InsightHistoryItem>>() {}.type
        )
    }
}