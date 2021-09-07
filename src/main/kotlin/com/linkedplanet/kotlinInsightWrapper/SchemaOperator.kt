package com.linkedplanet.kotlinInsightWrapper

import io.ktor.client.request.*

object SchemaOperator {

    suspend fun loadSchema(objectSchemas: List<String> = emptyList()): List<ObjectTypeSchema> {
        val schemas = InsightConfig.httpClient.get<List<ObjectTypeSchema>> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/objectschema/${InsightConfig.schemaId}/objecttypes/flat")
        }
        val filteredSchemas = schemas.takeIf { objectSchemas.isNotEmpty() }?.filter { objectSchemas.contains(it.name)  }?:schemas
        return filteredSchemas.map {
            val attributes = InsightConfig.httpClient.get<List<ObjectTypeSchemaAttribute>> {
                url("${InsightConfig.baseUrl}/rest/insight/1.0/objecttype/${it.id}/attributes")
            }
            it.attributes = attributes
            it
        }
    }



}