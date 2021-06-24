package com.linkedplanet.kotlininsightwrapper.ktor

import io.ktor.client.request.*

object SchemaOperator {

    suspend fun loadSchema(): List<ObjectTypeSchema> {
        val schemas = InsightConfig.httpClient.get<List<ObjectTypeSchema>> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/objectschema/${InsightConfig.schemaId}/objecttypes/flat")
        }
        return schemas.map {
            val attributes = InsightConfig.httpClient.get<List<ObjectTypeSchemaAttribute>> {
                url("${InsightConfig.baseUrl}/rest/insight/1.0/objecttype/${it.id}/attributes")
            }
            it.attributes = attributes
            it
        }
    }



}