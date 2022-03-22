package com.linkedplanet.kotlininsightwrapper.api.http

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.model.InsightSchemaDescription
import com.linkedplanet.kotlininsightwrapper.api.model.ObjectTypeSchema
import com.linkedplanet.kotlininsightwrapper.api.interfaces.InsightSchemaCacheOperatorInterface
import com.linkedplanet.kotlininsightwrapper.api.error.DomainError
import kotlinx.coroutines.runBlocking

object InsightConfig {

    lateinit var baseUrl: String
    lateinit var httpClient: BaseHttpClient
    lateinit var insightSchemaCacheOperator: InsightSchemaCacheOperatorInterface
    var objectSchemas: List<ObjectTypeSchema> = emptyList()
    var schemaDescriptionCache: List<InsightSchemaDescription> = emptyList()


    fun init(
        baseUrlIn: String,
        httpClientIn: BaseHttpClient,
        insightSchemaOperator: InsightSchemaCacheOperatorInterface
    ): Either<DomainError, Unit> {
        baseUrl = baseUrlIn
        httpClient = httpClientIn
        return runBlocking {
            insightSchemaOperator.updateSchemaCache()
        }
    }
}