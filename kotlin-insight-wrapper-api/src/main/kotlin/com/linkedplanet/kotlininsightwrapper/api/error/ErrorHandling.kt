package com.linkedplanet.kotlininsightwrapper.api.error

import java.lang.RuntimeException

class ObjectSchemaNameException(objectTypeName: String): RuntimeException("ObjectSchema with name [$objectTypeName] was not registered")
class ObjectSchemaIdException(objectTypeId: Int): RuntimeException("ObjectSchema with id [$objectTypeId] was not registered")