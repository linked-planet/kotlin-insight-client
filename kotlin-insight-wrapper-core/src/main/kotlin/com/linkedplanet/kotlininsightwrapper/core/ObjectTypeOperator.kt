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

import com.google.gson.reflect.TypeToken

object ObjectTypeOperator {

    suspend fun loadAllObjectTypeSchemas(): List<ObjectTypeSchema> {
        return InsightSchemaOperator.getSchemas().objectschemas.flatMap {
            loadObjectTypeSchemas(it.id)
        }
    }

    suspend fun loadObjectTypeSchemas(schemaId: Int): List<ObjectTypeSchema> {
        val schemas = getObjectTypeSchemas(schemaId)
        return schemas.map {
            parseObjectTypeSchema(it)
        }
    }

    suspend fun reloadObjectTypeSchema(schemaId: Int, name: String) {
        val schemas = getObjectTypeSchemas(schemaId).filter { it.name == name }
        schemas.firstOrNull()?.let {
            parseObjectTypeSchema(it)
        }?.apply {
            InsightConfig.objectSchemas =
                InsightConfig.objectSchemas.dropWhile { it.name == name } +
                this
        }
    }

    suspend fun reloadObjectTypeSchema(schemaId: Int, id: Int) {
        val schemas = getObjectTypeSchemas(schemaId).filter { it.id == id }
        schemas.firstOrNull()?.let {
            parseObjectTypeSchema(it)
        }?.apply {
            InsightConfig.objectSchemas =
                InsightConfig.objectSchemas.dropWhile { it.name == name } +
                        this
        }
    }

    suspend fun getObjectTypesBySchema(schemaId: Int): List<ObjectTypeSchema> =
        InsightConfig.httpClient.executeRestList<ObjectTypeSchema>(
            "GET",
            "rest/insight/1.0/objectschema/$schemaId/objecttypes/flat",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<ObjectTypeSchema>>() {}.type
        ).map {
            parseObjectTypeSchema(it)
        }

    private suspend fun getObjectTypeSchemas(schemaId: Int): List<ObjectTypeSchema> =
        InsightConfig.httpClient.executeRestList(
            "GET",
            "rest/insight/1.0/objectschema/$schemaId/objecttypes/flat",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<ObjectTypeSchema>>() {}.type
        )

    private suspend fun parseObjectTypeSchema(objectTypeSchema: ObjectTypeSchema): ObjectTypeSchema {
        val attributes = InsightConfig.httpClient.executeRestList<ObjectTypeSchemaAttribute>(
            "GET",
            "rest/insight/1.0/objecttype/${objectTypeSchema.id}/attributes",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<ObjectTypeSchemaAttribute>>() {}.type
        )
        objectTypeSchema.attributes = attributes
        return objectTypeSchema
    }


}