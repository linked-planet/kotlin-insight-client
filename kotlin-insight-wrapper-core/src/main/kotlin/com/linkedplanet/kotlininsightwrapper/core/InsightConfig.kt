package com.linkedplanet.kotlininsightwrapper.core

import kotlinx.coroutines.runBlocking

object InsightConfig {

    var schemaId: Int = 1
    lateinit var baseUrl: String
    lateinit var httpClient: BaseHttpClient
    lateinit var whiteListObjectTypes: List<String>
    var objectSchemas: List<ObjectTypeSchema> = emptyList()

    fun init(baseUrl: String, schemaId: Int, httpClient: BaseHttpClient, objectSchemas: List<String> = emptyList()) {
        this.baseUrl = baseUrl
        this.schemaId = schemaId
        this.whiteListObjectTypes = objectSchemas
        this.httpClient = httpClient
        this.objectSchemas = runBlocking { ObjectTypeOperator.loadAllObjectTypeSchemas() }
    }

    fun getObjectSchema(objectTypeName: String): ObjectTypeSchema =
        objectSchemas.firstOrNull { it.name == objectTypeName } ?: throw ObjectSchemaNameException(objectTypeName)

    fun getObjectSchema(objectTypeId: Int): ObjectTypeSchema =
        objectSchemas.firstOrNull { it.id == objectTypeId } ?: throw ObjectSchemaIdException(objectTypeId)
}