package com.linkedplanet.kotlininsightwrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.model.MyInsightEntry
import com.linkedplanet.kotlinhttpclient.error.DomainError

interface ObjectOperatorInterface {

    var RESULTS_PER_PAGE: Int

    suspend fun getObjects(objectTypeId: Int, withChildren: Boolean = false, pageFrom: Int = 1, pageTo: Int? = null, perPage: Int = RESULTS_PER_PAGE): Either<DomainError, List<MyInsightEntry>>

    suspend fun getObjectById(id: Int): Either<DomainError, MyInsightEntry?>

    suspend fun getObjectByKey(key: String): Either<DomainError, MyInsightEntry?>

    suspend fun getObjectByName(objectTypeId: Int, name: String): Either<DomainError, MyInsightEntry?>

    suspend fun getObjectsByIQL(
        objectTypeId: Int,
        withChildren: Boolean = false,
        iql: String,
        pageFrom: Int = 1,
        pageTo: Int? = null,
        perPage: Int = RESULTS_PER_PAGE
    ): Either<DomainError, List<MyInsightEntry>>

    suspend fun updateObject(obj: MyInsightEntry): Either<DomainError, MyInsightEntry>

    suspend fun deleteObject(id: Int): Boolean

    suspend fun createObject(objectTypeId: Int, func: (MyInsightEntry) -> Unit): Either<DomainError, MyInsightEntry>

    // PRIVATE DOWN HERE
}