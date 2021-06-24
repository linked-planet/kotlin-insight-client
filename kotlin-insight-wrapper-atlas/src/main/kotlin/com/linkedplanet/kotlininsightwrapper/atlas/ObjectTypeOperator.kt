package com.linkedplanet.kotlininsightwrapper.atlas

import com.atlassian.sal.api.net.Request
import com.google.gson.reflect.TypeToken
import org.apache.http.entity.ContentType

object ObjectTypeOperator {

    fun loadAllObjectTypeSchemas(): List<ObjectTypeSchema> {
        return InsightSchemaOperator.getSchemas().objectschemas.flatMap {
            loadObjectTypeSchemas(it.id)
        }
    }

    fun loadObjectTypeSchemas(schemaId: Int): List<ObjectTypeSchema> {
        val schemas = getObjectTypeSchemas(schemaId)
        return schemas.map {
            parseObjectTypeSchema(it)
        }
    }

    fun reloadObjectTypeSchema(schemaId: Int, name: String) {
        val schemas = getObjectTypeSchemas(schemaId).filter { it.name == name }
        schemas.firstOrNull()?.let {
            parseObjectTypeSchema(it)
        }?.apply {
            InsightConfig.objectSchemas =
                InsightConfig.objectSchemas.dropWhile { it.name == name } +
                this
        }
    }

    fun reloadObjectTypeSchema(schemaId: Int, id: Int) {
        val schemas = getObjectTypeSchemas(schemaId).filter { it.id == id }
        schemas.firstOrNull()?.let {
            parseObjectTypeSchema(it)
        }?.apply {
            InsightConfig.objectSchemas =
                InsightConfig.objectSchemas.dropWhile { it.name == name } +
                        this
        }
    }

    fun getObjectTypesBySchema(schemaId: Int): List<ObjectTypeSchema> =
        executeRestList<ObjectTypeSchema>(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/objectschema/$schemaId/objecttypes/flat",
            emptyMap(),
            null,
            ContentType.APPLICATION_JSON.toString(),
            object : TypeToken<List<ObjectTypeSchema>>() {}.type
        ).map {
            parseObjectTypeSchema(it)
        }

    private fun getObjectTypeSchemas(schemaId: Int): List<ObjectTypeSchema> =
        executeRestList(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/objectschema/$schemaId/objecttypes/flat",
            emptyMap(),
            null,
            ContentType.APPLICATION_JSON.toString(),
            object : TypeToken<List<ObjectTypeSchema>>() {}.type
        )

    private fun parseObjectTypeSchema(objectTypeSchema: ObjectTypeSchema): ObjectTypeSchema {
        val attributes = executeRestList<ObjectTypeSchemaAttribute>(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/objecttype/${objectTypeSchema.id}/attributes",
            emptyMap(),
            null,
            ContentType.APPLICATION_JSON.toString(),
            object : TypeToken<List<ObjectTypeSchemaAttribute>>() {}.type
        )
        objectTypeSchema.attributes = attributes
        return objectTypeSchema
    }


}