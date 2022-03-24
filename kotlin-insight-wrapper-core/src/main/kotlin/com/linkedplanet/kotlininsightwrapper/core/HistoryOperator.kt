package com.linkedplanet.kotlininsightwrapper.core

import arrow.core.Either
import arrow.core.computations.either
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlininsightwrapper.api.InsightConfig
import com.linkedplanet.kotlininsightwrapper.api.model.InsightHistoryItem
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlininsightwrapper.api.interfaces.HistoryOperatorInterface

object HistoryOperator: HistoryOperatorInterface {

    override suspend fun getHistory(objectId: Int): Either<DomainError, List<InsightHistoryItem>> = either {
        InsightConfig.httpClient.executeRestList<InsightHistoryItem>(
            "GET",
            "rest/insight/1.0/object/${objectId}/history",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<InsightHistoryItem>>() {}.type
        ).bind()
    }
}