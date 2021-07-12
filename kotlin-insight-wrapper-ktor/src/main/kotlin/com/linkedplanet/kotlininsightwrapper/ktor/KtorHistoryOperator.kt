package com.linkedplanet.kotlininsightwrapper.ktor

import com.linkedplanet.kotlininsightwrapper.core.BaseHistoryOperator
import com.linkedplanet.kotlininsightwrapper.core.InsightHistoryItem
import io.ktor.client.request.*
import io.ktor.http.*

object KtorHistoryOperator : BaseHistoryOperator(InsightConfig.baseUrl) {

    suspend fun getHistory(objectId: Int): List<InsightHistoryItem> {
        return InsightConfig.httpClient.get {
            url(getHistoryEndpoint(objectId))
            contentType(ContentType.Application.Json)
        }
    }
}