package com.linkedplanet.kotlininsightwrapper.jira

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.InsightHistoryItem
import com.linkedplanet.kotlininsightwrapper.api.interfaces.HistoryOperatorInterface
import com.linkedplanet.kotlininsightwrapper.core.DomainError

object HistoryOperator: HistoryOperatorInterface {

    override suspend fun getHistory(objectId: Int): Either<DomainError, List<InsightHistoryItem>> {
        TODO("Not yet implemented")
    }


}