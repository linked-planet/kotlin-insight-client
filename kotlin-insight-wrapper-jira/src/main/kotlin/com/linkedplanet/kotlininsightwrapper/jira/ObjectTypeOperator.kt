package com.linkedplanet.kotlininsightwrapper.jira

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.ObjectTypeSchema
import com.linkedplanet.kotlininsightwrapper.api.interfaces.ObjectTypeOperatorInterface
import com.linkedplanet.kotlininsightwrapper.core.DomainError

object ObjectTypeOperator: ObjectTypeOperatorInterface {
    override suspend fun loadAllObjectTypeSchemas(): Either<DomainError, List<ObjectTypeSchema>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadObjectTypeSchemas(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>> {
        TODO("Not yet implemented")
    }

    override suspend fun reloadObjectTypeSchema(schemaId: Int, name: String): Either<DomainError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun reloadObjectTypeSchema(schemaId: Int, id: Int): Either<DomainError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getObjectTypesBySchema(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>> {
        TODO("Not yet implemented")
    }

    override suspend fun getObjectTypeSchemas(schemaId: Int): Either<DomainError, List<ObjectTypeSchema>> {
        TODO("Not yet implemented")
    }

    override suspend fun parseObjectTypeSchema(objectTypeSchema: ObjectTypeSchema): Either<DomainError, ObjectTypeSchema> {
        TODO("Not yet implemented")
    }

}