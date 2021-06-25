package com.linkedplanet.kotlininsightwrapper.ktor

import com.linkedplanet.kotlininsightwrapper.core.*
import io.ktor.client.request.*
import io.ktor.http.*

object KtorObjectOperator :
    BaseObjectOperator(InsightConfig.baseUrl, InsightConfig.objectSchemas) {

    override suspend fun getObjects(objectTypeName: String): List<MyInsightEntry> {
        val objectType = getObjectType(objectTypeName)
        return InsightConfig.httpClient.get<InsightObjectEntries> {
            url("${iqlObjectsEndpoint}?objectSchemaId=${InsightConfig.schemaId}&iql=objectType=\"$objectTypeName\"&includeTypeAttributes=true")
        }.toValues(objectType)
    }

    override suspend fun getObject(objectTypeName: String, id: Int): MyInsightEntry? {
        val objectType = getObjectType(objectTypeName)
        return InsightConfig.httpClient.get<InsightObjectEntries> {
            url("${iqlObjectsEndpoint}?objectSchemaId=${InsightConfig.schemaId}&iql=objectType=\"$objectTypeName\" and objectId=$id&includeTypeAttributes=true")
        }.objectEntries.firstOrNull()?.toValue(objectType)
    }

    override suspend fun getObjectByName(
        objectTypeName: String,
        name: String
    ): MyInsightEntry? {
        val objectType = getObjectType(objectTypeName)
        return InsightConfig.httpClient.get<InsightObjectEntries> {
            url("${iqlObjectsEndpoint}?objectSchemaId=${InsightConfig.schemaId}&iql=objectType=\"$objectTypeName\" and Name=\"$name\"&includeTypeAttributes=true")
        }.objectEntries.firstOrNull()?.toValue(objectType)
    }

    override suspend fun getObjectsByIQL(
        objectTypeName: String,
        iql: String
    ): List<MyInsightEntry> {
        val objectType = getObjectType(objectTypeName)
        return InsightConfig.httpClient.get<InsightObjectEntries> {
            url("${iqlObjectsEndpoint}?objectSchemaId=${InsightConfig.schemaId}&iql=objectType=$objectTypeName and $iql&includeTypeAttributes=true")
        }.toValues(objectType)
    }

    override suspend fun createObject(obj: MyInsightEntry): MyInsightEntry {
        val schema = getSchemaByObject(obj)
        val objRefEditAttributes = obj.getEditReferences()
        val objEditAttributes = obj.getEditValues()
        val editItem = ObjectEditItem(
            obj.typeId,
            objEditAttributes + objRefEditAttributes
        )
        val response = InsightConfig.httpClient.post<ObjectUpdateResponse> {
            url("${objectEndpoint}/create")
            contentType(ContentType.Application.Json)
            body = editItem
        }
        obj.id = response.id
        return getObject(schema.name, response.id)!!
    }

    override suspend fun updateObject(obj: MyInsightEntry): MyInsightEntry {
        val schema = getSchemaByObject(obj)
        val objRefEditAttributes = obj.getEditReferences()
        val objEditAttributes = obj.getEditValues()
        val editItem = ObjectEditItem(
            obj.typeId,
            objEditAttributes + objRefEditAttributes
        )
        val response = InsightConfig.httpClient.put<ObjectUpdateResponse> {
            url("${objectEndpoint}/${obj.id}")
            contentType(ContentType.Application.Json)
            body = editItem
        }
        return getObject(schema.name, response.id)!!
    }

    override suspend fun deleteObject(id: Int): Boolean {
        val json = InsightConfig.httpClient.delete<String> {
            url("${objectEndpoint}/$id")
            contentType(ContentType.Application.Json)
        }
        return true
    }
}