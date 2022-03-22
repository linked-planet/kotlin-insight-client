/*
 * #%L
 * Zitsplatz
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

import arrow.core.Either
import arrow.core.computations.either
import com.linkedplanet.kotlininsightwrapper.api.model.InsightAttributeDescription
import com.linkedplanet.kotlininsightwrapper.api.http.InsightConfig
import com.linkedplanet.kotlininsightwrapper.api.model.InsightObjectTypeDescription
import com.linkedplanet.kotlininsightwrapper.api.model.InsightSchemaDescription
import com.linkedplanet.kotlininsightwrapper.api.error.DomainError
import com.linkedplanet.kotlininsightwrapper.api.interfaces.InsightSchemaCacheOperatorInterface
import org.joda.time.DateTime

object InsightSchemaCacheOperator: InsightSchemaCacheOperatorInterface {

    override var lastUpdate: DateTime? = null

    override suspend fun updateSchemaCache(): Either<DomainError, Unit> =
        either {
            val newCache = getSchemaCache().bind()
            val newSchema = ObjectTypeOperator.loadAllObjectTypeSchemas().bind()
            InsightConfig.objectSchemas = newSchema
            InsightConfig.schemaDescriptionCache = newCache
            lastUpdate = DateTime.now()
        }


    override suspend fun getSchemaCache(): Either<DomainError, List<InsightSchemaDescription>> =
        either {
            val schemas = InsightSchemaOperator.getSchemas().bind().objectschemas
            schemas.map { schema ->
                val objectTypes = ObjectTypeOperator.getObjectTypesBySchema(schema.id).bind()
                val objectTypeDescriptions = objectTypes.map {
                    val attributes = it.attributes.map { InsightAttributeDescription(it.id, it.name, it.defaultType?.name?:"") }
                    InsightObjectTypeDescription(it.id, it.name, attributes)
                }
                InsightSchemaDescription(schema.id, schema.name, objectTypeDescriptions)
            }
        }
}