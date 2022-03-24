package com.linkedplanet.kotlininsightwrapper.core

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatten
import com.google.gson.JsonParser
import com.linkedplanet.kotlinhttpclient.error.DomainError
import com.linkedplanet.kotlinhttpclient.api.http.GSON
import com.linkedplanet.kotlininsightwrapper.api.InsightConfig
import com.linkedplanet.kotlininsightwrapper.api.interfaces.ObjectOperatorInterface
import com.linkedplanet.kotlininsightwrapper.api.model.*

object ObjectOperator : ObjectOperatorInterface {

    override var RESULTS_PER_PAGE: Int = 25

    override suspend fun getObjects(
        objectTypeId: Int,
        withChildren: Boolean,
        pageFrom: Int,
        pageTo: Int?,
        perPage: Int
    ): Either<DomainError, List<MyInsightEntry>> {
        val iql = getIQLWithChildren(objectTypeId, withChildren)
        return getObjectsByPlainIQL(objectTypeId, iql, pageFrom, pageTo, perPage)
    }

    override suspend fun getObjectById(id: Int): Either<DomainError, MyInsightEntry?> {
        return getObjectByPlainIQL("objectId=$id")
    }

    override suspend fun getObjectByKey(key: String): Either<DomainError, MyInsightEntry?> {
        return getObjectByPlainIQL("Key=\"$key\"")
    }

    override suspend fun getObjectByName(objectTypeId: Int, name: String): Either<DomainError, MyInsightEntry?> {
        return getObjectByPlainIQL("objectTypeId=$objectTypeId AND Name=\"$name\"")
    }

    override suspend fun getObjectsByIQL(
        objectTypeId: Int,
        withChildren: Boolean,
        iql: String,
        pageFrom: Int,
        pageTo: Int?,
        perPage: Int
    ): Either<DomainError, List<MyInsightEntry>> {
        val fullIql = "${getIQLWithChildren(objectTypeId, withChildren)} AND $iql"
        return getObjectsByPlainIQL(
            objectTypeId,
            fullIql,
            pageFrom,
            pageTo,
            perPage
        )
    }

    override suspend fun updateObject(obj: MyInsightEntry): Either<DomainError, MyInsightEntry> {
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
        return response.map {
            getObjectById(it!!.id).map { it!! }
        }.flatten()
    }

    override suspend fun deleteObject(id: Int): Boolean =
        InsightConfig.httpClient.executeRestCall(
            "DELETE",
            "/rest/insight/1.0/object/$id",
            emptyMap(),
            null,
            "application/json"
        ).fold({ false }, { true })

    override suspend fun createObject(
        objectTypeId: Int,
        func: (MyInsightEntry) -> Unit
    ): Either<DomainError, MyInsightEntry> = either {
        val obj = createEmptyObject(objectTypeId)
        func(obj)
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
        obj.id = response.bind()!!.id
        getObjectById(obj.id).bind()!!
    }


    // PRIVATE DOWN HERE
    private suspend fun getObjectPages(
        objectTypeId: Int,
        iql: String,
        resultsPerPage: Int = RESULTS_PER_PAGE
    ): Either<DomainError, Int> {
        return InsightConfig.httpClient.executeGetCall(
            "rest/insight/1.0/iql/objects",
            mapOf(
                "iql" to "objectTypeId=\"$objectTypeId\" AND $iql",
                "includeTypeAttributes" to "true",
                "page" to "1",
                "resultsPerPage" to resultsPerPage.toString()
            )
        ).map { response ->
            JsonParser().parse(response).asJsonObject.get("pageSize").asInt
        }
    }

    private suspend fun getObjectsByPlainIQL(
        objectTypeId: Int,
        iql: String,
        pageFrom: Int,
        pageTo: Int?,
        perPage: Int
    ): Either<DomainError, List<MyInsightEntry>> = either {
        val maxPage = getObjectPages(objectTypeId, iql, perPage).bind()
        val lastPage = pageTo ?: maxPage
        lastPage.let { maxPageSize ->
            (pageFrom..maxPageSize).toList()
        }.flatMap { page ->
            InsightConfig.httpClient.executeRest<InsightObjectEntries>(
                "GET",
                "rest/insight/1.0/iql/objects",
                mapOf(
                    "iql" to iql,
                    "includeTypeAttributes" to "true",
                    "page" to "$page",
                    "resultPerPage" to perPage.toString()
                ),
                null,
                "application/json",
                InsightObjectEntries::class.java
            ).bind()?.toValues() ?: emptyList()
        }
    }

    private suspend fun getObjectByPlainIQL(
        iql: String
    ): Either<DomainError, MyInsightEntry?> = either {
        InsightConfig.httpClient.executeRest<InsightObjectEntries>(
            "GET",
            "rest/insight/1.0/iql/objects",
            mapOf(
                "iql" to iql,
                "includeTypeAttributes" to "true"
            ),
            null,
            "application/json",
            InsightObjectEntries::class.java
        ).bind()?.toValues()?.firstOrNull()
    }

    private fun InsightObject.toValue(): MyInsightEntry {
        val objectType = InsightConfig.objectSchemas.first { it.id == this.objectType.id }
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

    private suspend fun InsightObjectEntries.toValues(): List<MyInsightEntry> {
        return this.objectEntries.map {
            it.toValue()
        }
    }

    private fun getIQLWithChildren(objTypeId: Int, withChildren: Boolean): String =
        if (withChildren) {
            "objectType = objectTypeAndChildren(\"$objTypeId\")"
        } else {
            "objectTypeId=$objTypeId"
        }

    private suspend fun createEmptyObject(objectTypeId: Int): MyInsightEntry {
        val schema = InsightConfig.objectSchemas.first { it.id == objectTypeId }
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

    private suspend fun MyInsightEntry.getEditReferences(): List<ObjectEditItemAttribute> =
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

    private suspend fun MyInsightEntry.getEditValues(): List<ObjectEditItemAttribute> =
        this.attributes
            .filter { it.value.any { it.value != null } || this.isSelectField(it.attributeName) }
            .map {
                val values = it.value.map {
                    ObjectEditItemAttributeValue(
                        it.value
                    )
                }
                ObjectEditItemAttribute(
                    it.attributeId,
                    values
                )
            }

    private suspend fun MyInsightEntry.isSelectField(attributeName: String): Boolean =
        this.getAttributeType(attributeName)?.takeIf { it == "Select" }?.let { true } ?: false

}