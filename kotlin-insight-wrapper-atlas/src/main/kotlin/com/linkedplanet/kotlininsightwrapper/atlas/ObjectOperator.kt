package com.linkedplanet.kotlininsightwrapper.atlas

import com.atlassian.sal.api.net.Request
import com.linkedplanet.kotlininsightwrapper.core.*
import org.apache.http.entity.ContentType
import org.slf4j.LoggerFactory

object ObjectOperator {

    private val log = LoggerFactory.getLogger(ObjectOperator::class.java)

    fun getObjects(schemaId: Int, objectTypeId: Int): List<MyInsightEntry> {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeId)
        val objectType = InsightConfig.objectSchemas.first { it.id == objectTypeId }

        return executeRest<InsightObjectEntries>(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to schemaId.toString(),
                "iql" to "objectTypeId=\"$objectTypeId\"",
                "includeTypeAttributes" to "true"
            ),
            null,
            ContentType.APPLICATION_JSON.toString(),
            InsightObjectEntries::class.java
        )?.toValues(objectType)
            ?: emptyList()
    }

    fun getObjects(schemaId: Int, objectTypeName: String): List<MyInsightEntry> {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }

        return executeRest<InsightObjectEntries>(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to schemaId.toString(),
                "iql" to "objectType=\"$objectTypeName\"&includeTypeAttributes=true"
            ),
            null,
            ContentType.APPLICATION_JSON.toString(),
            InsightObjectEntries::class.java
        )?.toValues(objectType)
            ?: emptyList()
    }

    fun getObject(schemaId: Int, objectTypeName: String, id: Int): MyInsightEntry? {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        return executeRest<InsightObjectEntries>(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to schemaId.toString(),
                "iql" to "objectType=\"$objectTypeName\" and objectId=$id&includeTypeAttributes=true"
            ),
            null,
            ContentType.APPLICATION_JSON.toString(),
            InsightObjectEntries::class.java
        )?.objectEntries?.firstOrNull()?.toValue(objectType)
    }

    fun getObjectByName(
        schemaId: Int,
        objectTypeName: String,
        name: String
    ): MyInsightEntry? {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        return executeRest<InsightObjectEntries>(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to schemaId.toString(),
                "iql" to "objectType=\"$objectTypeName\" and Name=\"$name\"&includeTypeAttributes=true"
            ),
            null,
            ContentType.APPLICATION_JSON.toString(),
            InsightObjectEntries::class.java
        )?.objectEntries?.firstOrNull()?.toValue(objectType)
    }

    fun getObjectsByIQL(
        schemaId: Int,
        objectTypeName: String,
        iql: String
    ): List<MyInsightEntry> {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        val fullIql = if (iql.isNotEmpty()) {
            "objectType=$objectTypeName and $iql"
        } else {
            "objectType=$objectTypeName"
        }
        return executeRest<InsightObjectEntries>(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to schemaId.toString(),
                "iql" to fullIql,
                "includeTypeAttributes" to "true"
            ),
            null,
            ContentType.APPLICATION_JSON.toString(),
            InsightObjectEntries::class.java
        )?.toValues(objectType) ?: emptyList()
    }

    fun getObjectsByIQL(
        schemaId: Int,
        objectTypeId: Int,
        iql: String
    ): List<MyInsightEntry> {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeId)
        val objectType = InsightConfig.objectSchemas.first { it.id == objectTypeId }
        val fullIql = if (iql.isNotEmpty()) {
            "objectTypeId=$objectTypeId and $iql"
        } else {
            "objectTypeId=$objectTypeId"
        }
        return executeRest<InsightObjectEntries>(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to schemaId.toString(),
                "iql" to fullIql,
                "includeTypeAttributes" to "true"
            ),
            null,
            ContentType.APPLICATION_JSON.toString(),
            InsightObjectEntries::class.java
        )?.toValues(objectType) ?: emptyList()
    }

    fun createEmptyObject(schemaId: Int, objectTypeName: String): MyInsightEntry {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val schema = InsightConfig.objectSchemas.filter { it.name == objectTypeName }.first()
        val attributes = schema.attributes.map {
            MyInsightAttribute(
                it.id,
                it.name,
                emptyList()
            )
        }
        return MyInsightEntry(
            schema.id,
            -1,
            attributes,
            InsightConfig.objectSchemas
        )
    }

    fun createObject(schemaId: Int, obj: MyInsightEntry): MyInsightEntry {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, obj.typeId)
        val schema = InsightConfig.objectSchemas.first { it.id == obj.typeId }
        val objRefEditAttributes = obj.getEditReferences()
        val objEditAttributes = obj.getEditValues()
        val editItem = ObjectEditItem(
            obj.typeId,
            objEditAttributes + objRefEditAttributes
        )
        val response = executeRest<ObjectUpdateResponse>(
            InsightConfig.appLink!!,
            Request.MethodType.POST,
            "${InsightConfig.baseUrl}/rest/insight/1.0/object/create",
            emptyMap(),
            GSON.toJson(editItem),
            ContentType.APPLICATION_JSON.toString(),
            ObjectUpdateResponse::class.java
        )
        obj.id = response!!.id
        return getObject(schema.id, schema.name, response.id)!!
    }

    private fun MyInsightEntry.getEditReferences(): List<ObjectEditItemAttribute> =
        this.attributes
            .filter { it.value.any { it.referencedObject != null } }
            .map {
                val values = it.value.map {
                    ObjectEditItemAttributeValue(
                        it.referencedObject!!.id
                    )
                }
                ObjectEditItemAttribute(
                    it.attributeId,
                    values
                )
            }

    private fun MyInsightEntry.getEditValues(): List<ObjectEditItemAttribute> =
        this.attributes
            .filter { it.value.any { it.value != null } || this.isSelectField(it.attributeName) }
            .map {
                val values = it.value.mapNotNull {
                    ObjectEditItemAttributeValue(
                        it.value
                    )
                }
                ObjectEditItemAttribute(
                    it.attributeId,
                    values
                )
            }

    private fun MyInsightEntry.isSelectField(attributeName: String): Boolean =
        this.getAttributeType(attributeName)?.takeIf { it == "Select" }?.let { true } ?: false

    fun updateObject(schemaId: Int, obj: MyInsightEntry): MyInsightEntry {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, obj.typeId)
        val schema = InsightConfig.objectSchemas.first { it.id == obj.typeId }
        val objRefEditAttributes = obj.getEditReferences()
        val objEditAttributes = obj.getEditValues()
        val editItem = ObjectEditItem(
            obj.typeId,
            objEditAttributes + objRefEditAttributes
        )
        val response =
            executeRest<ObjectUpdateResponse>(
                InsightConfig.appLink!!,
                Request.MethodType.PUT,
                "${InsightConfig.baseUrl}/rest/insight/1.0/object/${obj.id}",
                emptyMap(),
                GSON.toJson(editItem),
                ContentType.APPLICATION_JSON.toString(),
                ObjectUpdateResponse::class.java
            )
        return getObject(schema.id, schema.name, response!!.id)!!
    }

    fun deleteObject(id: Int): Boolean {
        val json =
            executeRest<String>(
                InsightConfig.appLink!!,
                Request.MethodType.DELETE,
                "${InsightConfig.baseUrl}/rest/insight/1.0/object/$id",
                emptyMap(),
                null,
                ContentType.APPLICATION_JSON.toString(),
                String::class.java
            )
        return true
    }

    private fun InsightObject.toValue(objectType: ObjectTypeSchema): MyInsightEntry {
        val attributes = this.attributes.map {
            val attributeId = it.objectTypeAttributeId
            MyInsightAttribute(
                it.objectTypeAttributeId,
                objectType.attributes.firstOrNull { it.id == attributeId }?.name ?: "",
                it.objectAttributeValues
            )
        }
        return MyInsightEntry(
            objectType.id,
            this.id,
            attributes,
            InsightConfig.objectSchemas
        )
    }

    private fun InsightObjectEntries.toValues(objectType: ObjectTypeSchema): List<MyInsightEntry> {
        return this.objectEntries.map {
            it.toValue(objectType)
        }
    }
}