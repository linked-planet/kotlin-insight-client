package com.linkedplanet.kotlinInsightWrapper

import com.linkedplanet.kotlinInsightWrapper.ObjectOperator.getEditReferences
import com.linkedplanet.kotlinInsightWrapper.ObjectOperator.getEditValues
import io.ktor.client.request.*
import io.ktor.http.*

object ObjectOperator {

    suspend fun getObjects(objectTypeName: String): List<MyInsightEntry> {
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        return InsightConfig.httpClient.get<InsightObjectEntries> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects?objectSchemaId=${InsightConfig.schemaId}&iql=objectType=\"$objectTypeName\"&includeTypeAttributes=true")
        }.toValues(objectType)
    }

    suspend fun getObject(objectTypeName: String, id: Int): MyInsightEntry? {
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        return InsightConfig.httpClient.get<InsightObjectEntries> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects?objectSchemaId=${InsightConfig.schemaId}&iql=objectType=\"$objectTypeName\" and objectId=$id&includeTypeAttributes=true")
        }.objectEntries.firstOrNull()?.toValue(objectType)
    }

    suspend fun getObjectByName(
        objectTypeName: String,
        name: String
    ): MyInsightEntry? {
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        return InsightConfig.httpClient.get<InsightObjectEntries> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects?objectSchemaId=${InsightConfig.schemaId}&iql=objectType=\"$objectTypeName\" and Name=\"$name\"&includeTypeAttributes=true")
        }.objectEntries.firstOrNull()?.toValue(objectType)
    }

    suspend fun getObjectsByIQL(
        objectTypeName: String,
        iql: String
    ): List<MyInsightEntry> {
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        return InsightConfig.httpClient.get<InsightObjectEntries> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/iql/objects?objectSchemaId=${InsightConfig.schemaId}&iql=objectType=$objectTypeName and $iql&includeTypeAttributes=true")
        }.toValues(objectType)
    }

    fun createEmptyObject(objectTypeName: String): MyInsightEntry {
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
            attributes
        )
    }

    suspend fun createObject(obj: MyInsightEntry): MyInsightEntry {
        val schema = InsightConfig.objectSchemas.first { it.id == obj.typeId }
        val objRefEditAttributes = obj.getEditReferences()
        val objEditAttributes = obj.getEditValues()
        val editItem = ObjectEditItem(
            obj.typeId,
            objEditAttributes + objRefEditAttributes
        )
        val response = InsightConfig.httpClient.post<ObjectUpdateResponse> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/object/create")
            contentType(ContentType.Application.Json)
            body = editItem
        }
        obj.id = response.id
        return getObject(schema.name, response.id)!!
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
            .filter { it.value.any { it.value != null } }
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

    suspend fun updateObject(obj: MyInsightEntry): MyInsightEntry {
        val schema = InsightConfig.objectSchemas.first { it.id == obj.typeId }
        val objRefEditAttributes = obj.getEditReferences()
        val objEditAttributes = obj.getEditValues()
        val editItem = ObjectEditItem(
            obj.typeId,
            objEditAttributes + objRefEditAttributes
        )
        val response = InsightConfig.httpClient.put<ObjectUpdateResponse> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/object/${obj.id}")
            contentType(ContentType.Application.Json)
            body = editItem
        }
        return getObject(schema.name, response.id)!!
    }

    suspend fun deleteObject(id: Int): Boolean {
        val json = InsightConfig.httpClient.delete<String> {
            url("${InsightConfig.baseUrl}/rest/insight/1.0/object/$id")
            contentType(ContentType.Application.Json)
        }
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
            attributes
        )
    }

    private fun InsightObjectEntries.toValues(objectType: ObjectTypeSchema): List<MyInsightEntry> {
        return this.objectEntries.map {
            it.toValue(objectType)
        }
    }
}