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
package com.linkedplanet.kotlininsightwrapper.jira

import arrow.core.Either
import arrow.core.right
import com.linkedplanet.kotlininsightwrapper.api.*
import com.linkedplanet.kotlininsightwrapper.api.interfaces.InsightSchemaCacheOperatorInterface
import com.linkedplanet.kotlininsightwrapper.core.*
import com.linkedplanet.kotlininsightwrapper.jira.utils.getFacade
import com.linkedplanet.kotlininsightwrapper.jira.utils.objectType
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectSchemaFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade
import org.joda.time.DateTime

object InsightSchemaCacheOperator: InsightSchemaCacheOperatorInterface {

    var schemaDescriptions: List<InsightSchemaDescription> = emptyList()
    var schemas: List<ObjectTypeSchema> = emptyList()

    override var lastUpdate: DateTime?
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun updateSchemaCache(): Either<DomainError, Unit> {
        val schemaDescriptionsIn: List<InsightSchemaDescription> = getFacade<ObjectSchemaFacade>().findObjectSchemaBeans().map { objectSchemaBean ->
            val objectTypes: List<InsightObjectTypeDescription> = getFacade<ObjectTypeFacade>().findObjectTypeBeans(objectSchemaBean.id).map { objectTypeBean ->
                val attributes: List<InsightAttributeDescription> = objectTypeBean.id.objectType.attributes.map { attributeBean ->
                    InsightAttributeDescription(
                        attributeBean.id,
                        attributeBean.name,
                        attributeBean.defaultType?.name?:""
                    )
                }
                InsightObjectTypeDescription(
                    objectTypeBean.id,
                    objectTypeBean.name,
                    attributes
                )
            }

            InsightSchemaDescription(
                objectSchemaBean.id,
                objectSchemaBean.name,
                objectTypes
            )
        }
        schemaDescriptions = schemaDescriptionsIn

        val schemasIn: List<ObjectTypeSchema> = getFacade<ObjectSchemaFacade>().findObjectSchemaBeans().flatMap { objectSchemaBean ->
            val objectTypes = getFacade<ObjectTypeFacade>().findObjectTypeBeans(objectSchemaBean.id).map { objectTypeBean ->
                val attributes = objectTypeBean.id.objectType.attributes.map { attributeBean ->
                    ObjectTypeSchemaAttribute(
                        attributeBean.id,
                        attributeBean.name,
                        ObjectTypeAttributeDefaultType(
                            attributeBean.defaultType?.id?:-1,
                            attributeBean.defaultType?.name?:""
                        ),
                        attributeBean.options
                    )
                }
                ObjectTypeSchema(
                    objectTypeBean.id,
                    objectTypeBean.name,
                    attributes
                )
            }
            objectTypes
        }

        schemas = schemasIn

        return Unit.right()
    }

    override suspend fun getSchemaCache(): Either<DomainError, List<InsightSchemaDescription>> {
        return this.schemaDescriptions.right()
    }

}