package com.linkedplanet.kotlininsightwrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlininsightwrapper.api.model.ObjectTypeSchema

interface ObjectTypeOperatorInterface {

    suspend fun loadAllObjectTypeSchemas(): Either<DomainError, List<ObjectTypeSchema>>

    suspend fun loadObjectTypeSchemas(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>>

    suspend fun reloadObjectTypeSchema(schemaId: Int, name: String): Either<DomainError, Unit>

    suspend fun reloadObjectTypeSchema(schemaId: Int, id: Int): Either<DomainError, Unit>

    suspend fun getObjectTypesBySchema(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>>

    suspend fun getObjectTypesBySchemaAndRootObjectType(
        schemaId: Int,
        rootObjectTypeId: Int
    ): Either<DomainError, List<ObjectTypeSchema>>

    suspend fun getObjectTypeSchemas(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>>

    suspend fun populateObjectTypeSchemaAttributes(objectTypeSchema: ObjectTypeSchema): Either<DomainError, ObjectTypeSchema>
}