package com.linkedplanet.kotlininsightwrapper.core

abstract class BaseObjectOperator(val baseUrl: String, private val objectSchemas: List<ObjectTypeSchema>) {

    val objectEndpoint: String = "rest/insight/1.0/object"
        get() = "$baseUrl/$field"

    val iqlObjectsEndpoint: String = "rest/insight/1.0/iql/objects"
        get() = "$baseUrl/$field"

    abstract suspend fun getObjects(objectTypeName: String): List<MyInsightEntry>

    abstract suspend fun getObject(objectTypeName: String, id: Int): MyInsightEntry?

    abstract suspend fun getObjectByName(
        objectTypeName: String,
        name: String
    ): MyInsightEntry?

    abstract suspend fun getObjectsByIQL(
        objectTypeName: String,
        iql: String
    ): List<MyInsightEntry>

    abstract suspend fun createObject(obj: MyInsightEntry): MyInsightEntry

    abstract suspend fun updateObject(obj: MyInsightEntry): MyInsightEntry

    abstract suspend fun deleteObject(id: Int): Boolean

    fun createEmptyObject(objectTypeName: String): MyInsightEntry {
        val schema = getSchemaByObjectTypeName(objectTypeName)
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
            attributes,
            objectSchemas
        )
    }

    protected fun getObjectType(objectTypeName: String) =
        objectSchemas.first { it.name == objectTypeName }

    protected fun getSchemaByObject(obj: MyInsightEntry) =
        objectSchemas.first { it.id == obj.typeId }

    private fun getSchemaByObjectTypeName(objectTypeName: String) =
        objectSchemas.filter { it.name == objectTypeName }.first()

    // MyInsightEntry
    // --------------
    protected fun MyInsightEntry.getEditReferences(): List<ObjectEditItemAttribute> =
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

    protected fun MyInsightEntry.getEditValues(): List<ObjectEditItemAttribute> =
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

    private fun MyInsightEntry.isSelectField(attributeName: String): Boolean =
        this.getAttributeType(attributeName)?.takeIf { it == "Select" }?.let { true } ?: false

    // InsightObject
    // -------------
    protected fun InsightObject.toValue(objectType: ObjectTypeSchema): MyInsightEntry {
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
            attributes,
            objectSchemas
        )
    }

    protected fun InsightObjectEntries.toValues(objectType: ObjectTypeSchema): List<MyInsightEntry> {
        return this.objectEntries.map {
            it.toValue(objectType)
        }
    }
}