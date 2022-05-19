package com.linkedplanet.kotlininsightwrapper.core

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import com.google.gson.reflect.TypeToken
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlininsightwrapper.api.InsightConfig
import com.linkedplanet.kotlininsightwrapper.api.interfaces.ObjectTypeOperatorInterface
import com.linkedplanet.kotlininsightwrapper.api.model.*

object ObjectTypeOperator : ObjectTypeOperatorInterface {
    override suspend fun loadAllObjectTypeSchemas(): Either<DomainError, List<ObjectTypeSchema>> = either {
        InsightSchemaOperator.getSchemas().bind().objectschemas.flatMap {
            loadObjectTypeSchemas(it.id).bind()
        }
    }

    override suspend fun loadObjectTypeSchemas(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>> = either {
        val schemas = getObjectTypeSchemas(schemaId).bind()
        schemas.map {
            populateObjectTypeSchemaAttributes(it).bind()
        }
    }

    override suspend fun reloadObjectTypeSchema(schemaId: Int, name: String): Either<DomainError, Unit> = either {
        val schemas = getObjectTypeSchemas(schemaId).bind().filter { it.name == name }
        schemas.firstOrNull()?.let {
            populateObjectTypeSchemaAttributes(it).bind()
        }?.apply {
            InsightConfig.objectSchemas =
                InsightConfig.objectSchemas.dropWhile { it.name == name } +
                        this
        }
    }

    override suspend fun reloadObjectTypeSchema(schemaId: Int, id: Int): Either<DomainError, Unit> = either {
        val schemas = getObjectTypeSchemas(schemaId).bind().filter { it.id == id }
        schemas.firstOrNull()?.let {
            populateObjectTypeSchemaAttributes(it).bind()
        }?.apply {
            InsightConfig.objectSchemas =
                InsightConfig.objectSchemas.dropWhile { it.name == name } + this
        }
    }

    override suspend fun getObjectTypesBySchemaAndRootObjectType(
        schemaId: Int,
        rootObjectTypeId: Int
    ): Either<DomainError, List<ObjectTypeSchema>> = either {
        val allObjectTypes = getObjectTypesBySchema(schemaId).bind()
        allObjectTypes
            .firstOrNull { it.id == rootObjectTypeId }
            ?.let { rootObject ->
                listOf(rootObject).plus(findObjectTypeChildren(allObjectTypes, rootObjectTypeId))
            }
            ?: ObjectTypeNotFoundError().left().bind()
    }

    override suspend fun getObjectTypesBySchema(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>> = either {
        InsightConfig.httpClient.executeRestList<ObjectTypeSchema>(
            "GET",
            "rest/insight/1.0/objectschema/$schemaId/objecttypes/flat",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<ObjectTypeSchema>>() {}.type
        ).bind().map {
            populateObjectTypeSchemaAttributes(it).bind()
        }
    }

    override suspend fun getObjectTypeSchemas(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>> = either {
        val result: Either<DomainError, List<ObjectTypeSchema>> = InsightConfig.httpClient.executeRestList(
            "GET",
            "rest/insight/1.0/objectschema/$schemaId/objecttypes/flat",
            emptyMap(),
            null,
            "application/json",
            object : TypeToken<List<ObjectTypeSchema>>() {}.type
        )
        result.bind()
    }

    override suspend fun populateObjectTypeSchemaAttributes(objectTypeSchema: ObjectTypeSchema): Either<DomainError, ObjectTypeSchema> =
        either {
            val attributes = InsightConfig.httpClient.executeRestList<ObjectTypeSchemaAttribute>(
                "GET",
                "rest/insight/1.0/objecttype/${objectTypeSchema.id}/attributes",
                emptyMap(),
                null,
                "application/json",
                object : TypeToken<List<ObjectTypeSchemaAttribute>>() {}.type
            ).bind()
            objectTypeSchema.attributes = attributes
            objectTypeSchema
        }

    private fun findObjectTypeChildren(
        objectTypes: List<ObjectTypeSchema>,
        rootObjectTypeId: Int
    ): List<ObjectTypeSchema> {
        val directChildren = objectTypes.filter { it.parentObjectTypeId == rootObjectTypeId }
        val transitiveChildren = directChildren.flatMap { child -> findObjectTypeChildren(objectTypes, child.id) }
        return directChildren.plus(transitiveChildren)
    }
}