package com.linkedplanet.kotlinInsightWrapper

import io.ktor.client.*
import kotlinx.coroutines.runBlocking

object InsightConfig {

    var baseUrl: String = "https://insight-api.riada.io"
    var schemaId: Int = 1
    var httpClient: HttpClient = httpClient("", "")
    var objectSchemas: List<ObjectTypeSchema> = emptyList()

    fun init(baseUrl: String, schemaId: Int, username: String, password: String, objectSchemas: List<String> = emptyList()) {
        this.baseUrl = baseUrl
        this.schemaId = schemaId
        this.httpClient = httpClient(username, password)
        this.objectSchemas = runBlocking { SchemaOperator.loadSchema(objectSchemas) }
    }

    fun getObjectSchema(objectTypeName: String): ObjectTypeSchema =
        objectSchemas.firstOrNull { it.name == objectTypeName } ?: throw ObjectSchemaNameException(objectTypeName)

    fun getObjectSchema(objectTypeId: Int): ObjectTypeSchema =
        objectSchemas.firstOrNull { it.id == objectTypeId } ?: throw ObjectSchemaIdException(objectTypeId)
}