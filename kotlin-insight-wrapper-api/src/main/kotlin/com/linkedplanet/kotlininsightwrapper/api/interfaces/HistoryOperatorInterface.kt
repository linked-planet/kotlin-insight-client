package com.linkedplanet.kotlininsightwrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.model.InsightHistoryItem
import com.linkedplanet.kotlinhttpclient.error.DomainError

interface HistoryOperatorInterface {

    suspend fun getHistory(objectId: Int): Either<DomainError, List<InsightHistoryItem>>

}