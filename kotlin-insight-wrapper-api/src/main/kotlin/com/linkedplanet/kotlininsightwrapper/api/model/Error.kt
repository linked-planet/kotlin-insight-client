package com.linkedplanet.kotlininsightwrapper.api.model

import com.linkedplanet.kotlinhttpclient.error.DomainError

class ObjectTypeNotFoundError : DomainError("Nicht gefunden", "Der ObjectType mit der angegebenen Id wurde nicht gefunden.")