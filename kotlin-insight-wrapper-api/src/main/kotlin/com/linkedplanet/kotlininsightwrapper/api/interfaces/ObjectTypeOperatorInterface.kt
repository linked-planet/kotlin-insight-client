package com.linkedplanet.kotlininsightwrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.model.ObjectTypeSchema
import com.linkedplanet.kotlinhttpclient.error.DomainError

interface ObjectTypeOperatorInterface {

    suspend fun loadAllObjectTypeSchemas(): Either<DomainError, List<ObjectTypeSchema>>

    suspend fun loadObjectTypeSchemas(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>>

    suspend fun reloadObjectTypeSchema(schemaId: Int, name: String): Either<DomainError, Unit>

    suspend fun reloadObjectTypeSchema(schemaId: Int, id: Int): Either<DomainError, Unit>

    suspend fun getObjectTypesBySchema(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>>

    // PRIVATE DOWN HERE
    suspend fun getObjectTypeSchemas(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>>

    suspend fun parseObjectTypeSchema(objectTypeSchema: ObjectTypeSchema): Either<DomainError, ObjectTypeSchema>


}