/*
 * #%L
 * insight-reporting
 * %%
 * Copyright (C) 2018 The Plugin Authors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.linkedplanet.kotlininsightwrapper.core

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

object ObjectOperator {

    suspend fun getObjectPages(objectTypeName: String, resultsPerPage: Int = 25): Int {
        val response = InsightConfig.httpClient.executeGetCall(
            "rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to "${InsightConfig.schemaId}",
                "iql" to "objectType=\"$objectTypeName\"",
                "includeTypeAttributes" to "true",
                "page" to "1",
                "resultPerPage" to "$resultsPerPage"
            )
        )
        return JsonParser().parse(response).asJsonObject.get("pageSize").asInt
    }

    suspend fun getObjectIqlPages(objectTypeName: String, iql: String, resultsPerPage: Int = 25): Int {
        val response = InsightConfig.httpClient.executeGetCall(
            "rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to "${InsightConfig.schemaId}",
                "iql" to "objectType=$objectTypeName and $iql",
                "includeTypeAttributes" to "true",
                "page" to "1",
                "resultPerPage" to "$resultsPerPage"
            )
        )
        return JsonParser().parse(response).asJsonObject.get("pageSize").asInt
    }

    suspend fun getObjects(
        schemaId: Int,
        objectTypeId: Int,
        page: Int = -1,
        resultsPerPage: Int = 25
    ): List<MyInsightEntry> {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeId)
        val objectType = InsightConfig.objectSchemas.first { it.id == objectTypeId }

        return if (page < 0) {
            var resultList = emptyList<MyInsightEntry>()
            var index = 1
            var max = 0
            do {
                val response = InsightConfig.httpClient.executeGetCall(
                    "rest/insight/1.0/iql/objects",
                    mapOf(
                        "objectSchemaId" to schemaId.toString(),
                        "iql" to "objectTypeId=\"$objectTypeId\"",
                        "includeTypeAttributes" to "true",
                        "page" to "$index",
                        "resultPerPage" to "$resultsPerPage"
                    )
                )
                val result =
                    GsonBuilder().create().fromJson<InsightObjectEntries>(response, InsightObjectEntries::class.java)
                        .toValues(objectType)
                resultList = resultList + result
                index += 1
                max = JsonParser().parse(response).asJsonObject.get("pageSize").asInt
            } while (index <= max)
            return resultList
        } else {
            InsightConfig.httpClient.executeRest<InsightObjectEntries>(
                "GET",
                "rest/insight/1.0/iql/objects",
                mapOf(
                    "objectSchemaId" to schemaId.toString(),
                    "iql" to "objectTypeId=\"$objectTypeId\"",
                    "includeTypeAttributes" to "true",
                    "page" to "$page",
                    "resultPerPage" to "$resultsPerPage"
                ),
                null,
                "application/json",
                InsightObjectEntries::class.java
            )?.toValues(objectType)
                ?: emptyList()
        }
    }

    suspend fun getObjects(
        schemaId: Int,
        objectTypeName: String,
        page: Int = -1,
        resultsPerPage: Int = 25
    ): List<MyInsightEntry> {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }

        return getObjects(schemaId, objectType.id, page, resultsPerPage)
    }

    suspend fun getObject(schemaId: Int, objectTypeName: String, id: Int): MyInsightEntry? {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        return InsightConfig.httpClient.executeRest<InsightObjectEntries>(
            "GET",
            "rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to schemaId.toString(),
                "iql" to "objectType=\"$objectTypeName\" and objectId=$id",
                "includeTypeAttributes" to "true"
            ),
            null,
            "application/json",
            InsightObjectEntries::class.java
        )?.objectEntries?.firstOrNull()?.toValue(objectType)
    }

    suspend fun getObjectByName(
        schemaId: Int,
        objectTypeName: String,
        name: String
    ): MyInsightEntry? {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        return InsightConfig.httpClient.executeRest<InsightObjectEntries>(
            "GET",
            "rest/insight/1.0/iql/objects",
            mapOf(
                "objectSchemaId" to schemaId.toString(),
                "iql" to "objectType=\"$objectTypeName\" and Name=\"$name\"",
                "includeTypeAttributes" to "true"
            ),
            null,
            "application/json",
            InsightObjectEntries::class.java
        )?.objectEntries?.firstOrNull()?.toValue(objectType)
    }

    suspend fun getObjectsByIQL(
        schemaId: Int,
        objectTypeName: String,
        iql: String,
        page: Int = -1,
        resultsPerPage: Int = 25
    ): List<MyInsightEntry> {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeName)
        val objectType = InsightConfig.objectSchemas.first { it.name == objectTypeName }
        val fullIql = if (iql.isNotEmpty()) {
            "objectType=$objectTypeName and $iql"
        } else {
            "objectType=$objectTypeName"
        }

        return if (page < 0) {
            var resultList = emptyList<MyInsightEntry>()
            var index = 1
            var max = 0
            do {
                val response = InsightConfig.httpClient.executeGetCall(
                    "rest/insight/1.0/iql/objects",
                    mapOf(
                        "objectSchemaId" to schemaId.toString(),
                        "iql" to "$fullIql",
                        "includeTypeAttributes" to "true",
                        "page" to "$index",
                        "resultPerPage" to "$resultsPerPage"
                    )
                )
                val result =
                    GsonBuilder().create().fromJson<InsightObjectEntries>(response, InsightObjectEntries::class.java)
                        .toValues(objectType)
                resultList = resultList + result
                index += 1
                max = JsonParser().parse(response).asJsonObject.get("pageSize").asInt
            } while (index <= max)
            return resultList
        } else {
            InsightConfig.httpClient.executeRest<InsightObjectEntries>(
                "GET",
                "rest/insight/1.0/iql/objects",
                mapOf(
                    "objectSchemaId" to schemaId.toString(),
                    "iql" to "$fullIql",
                    "includeTypeAttributes" to "true",
                    "page" to "$page",
                    "resultPerPage" to "$resultsPerPage"
                ),
                null,
                "application/json",
                InsightObjectEntries::class.java
            )?.toValues(objectType)
                ?: emptyList()
        }
    }

    suspend fun getObjectsByIQL(
        schemaId: Int,
        objectTypeId: Int,
        iql: String,
        page: Int = -1,
        resultsPerPage: Int = 25
    ): List<MyInsightEntry> {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, objectTypeId)
        val objectType = InsightConfig.objectSchemas.first { it.id == objectTypeId }
        return getObjectsByIQL(schemaId, objectType.name, iql, page, resultsPerPage)
    }

    suspend fun createEmptyObject(schemaId: Int, objectTypeName: String): MyInsightEntry {
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
            attributes
        )
    }

    suspend fun createObject(schemaId: Int, obj: MyInsightEntry): MyInsightEntry {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, obj.typeId)
        val schema = InsightConfig.objectSchemas.first { it.id == obj.typeId }
        val objRefEditAttributes = obj.getEditReferences()
        val objEditAttributes = obj.getEditValues()
        val editItem = ObjectEditItem(
            obj.typeId,
            objEditAttributes + objRefEditAttributes
        )
        val response = InsightConfig.httpClient.executeRest<ObjectUpdateResponse>(
            "POST",
            "rest/insight/1.0/object/create",
            emptyMap(),
            GSON.toJson(editItem),
            "application/json",
            ObjectUpdateResponse::class.java
        )
        obj.id = response!!.id
        return getObject(schemaId, schema.name, response.id)!!
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

    suspend fun updateObject(schemaId: Int, obj: MyInsightEntry): MyInsightEntry {
        ObjectTypeOperator.reloadObjectTypeSchema(schemaId, obj.typeId)
        val schema = InsightConfig.objectSchemas.first { it.id == obj.typeId }
        val objRefEditAttributes = obj.getEditReferences()
        val objEditAttributes = obj.getEditValues()
        val editItem = ObjectEditItem(
            obj.typeId,
            objEditAttributes + objRefEditAttributes
        )
        val response =
            InsightConfig.httpClient.executeRest<ObjectUpdateResponse>(
                "PUT",
                "rest/insight/1.0/object/${obj.id}",
                emptyMap(),
                GSON.toJson(editItem),
                "application/json",
                ObjectUpdateResponse::class.java
            )
        return getObject(schemaId, schema.name, response!!.id)!!
    }

    suspend fun deleteObject(id: Int): Boolean {
        val json =
            InsightConfig.httpClient.executeRestCall(
                "DELETE",
                "rest/insight/1.0/object/$id",
                emptyMap(),
                null,
                "application/json"
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
            attributes
        )
    }

    private fun InsightObjectEntries.toValues(objectType: ObjectTypeSchema): List<MyInsightEntry> {
        return this.objectEntries.map {
            it.toValue(objectType)
        }
    }
}