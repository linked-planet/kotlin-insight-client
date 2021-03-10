package com.linkedplanet.kotlinInsightWrapper

import io.ktor.client.request.*
import io.ktor.http.*

object HistoryOperator {

    suspend fun getHistory(objectId: Int): List<InsightHistoryItem> {
        return InsightConfig.httpClient.get {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/object/${objectId}/history")
            contentType(ContentType.Application.Json)
        }
    }
}