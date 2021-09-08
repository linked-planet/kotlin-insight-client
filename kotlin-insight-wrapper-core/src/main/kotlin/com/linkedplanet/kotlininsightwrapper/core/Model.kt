/*
 * #%L
 * insight-reporting
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

import com.linkedplanet.kotlinInsightWrapper.AttachmentOperator
import org.joda.time.DateTime
import java.util.Collections.emptyList

data class MyInsightEntry(
    val typeId: Int,
    var id: Int,
    var attributes: List<MyInsightAttribute>
)

fun MyInsightEntry.getAttributeNames(): List<ObjectTypeSchemaAttribute> {
    val schema = InsightConfig.objectSchemas.first { it.id == this.typeId }
    return schema.attributes
}

fun MyInsightEntry.getAttributeType(name: String): String? {
    val schema = InsightConfig.objectSchemas.first { it.id == this.typeId }
    return schema.attributes.firstOrNull { it.name == name }?.defaultType?.name
}

fun MyInsightEntry.isReferenceAttribute(name: String): Boolean =
    this.attributes.filter { it.attributeName == name }.any { it.value.any { it.referencedObject != null } }

fun MyInsightEntry.isValueAttribute(name: String): Boolean =
    this.attributes.filter { it.attributeName == name }.any { it.value.any { it.value != null } }

fun MyInsightEntry.exists(name: String): Boolean =
    this.attributes.firstOrNull { it.attributeName == name } != null

fun MyInsightEntry.getStringValue(name: String): String? =
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?.firstOrNull()
        ?.value
        ?.toString()

fun MyInsightEntry.clearValueList(name: String) {
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value = emptyList()
}

fun MyInsightEntry.getValueList(name: String): List<Any> {
    return this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?.mapNotNull { it.value }
        ?:emptyList<Any>()
}

fun MyInsightEntry.setValueList(name: String, values: List<Any?>) {
    val attribute = this.attributes
        .firstOrNull { it.attributeName == name }
    if(attribute == null){
        this.createAttribute(name)
    }
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value = values.map {
        ObjectAttributeValue(
            it,
            "",
            null
        )
    }
}

fun MyInsightEntry.removeValue(name: String, value: Any?) {
    val values = this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?: emptyList()
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value = values.filter { it.value != value }
}

private fun MyInsightEntry.createAttribute(name: String) {
    val schema = InsightConfig.objectSchemas.first { it.id == this.typeId }
    val attribute = schema.attributes.firstOrNull { it.name == name }
    if (attribute != null) {
        this.attributes = this.attributes + MyInsightAttribute(
            attribute.id,
            attribute.name,
            emptyList()
        )
    }
}

fun MyInsightEntry.addValue(name: String, value: Any?) {
    val exists = this.attributes
        .firstOrNull { it.attributeName == name }
    if (exists == null) {
        this.createAttribute(name)
    }
    val values = this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?: emptyList()
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value = values + ObjectAttributeValue(
        value,
        "",
        null
    )
}

fun MyInsightEntry.setValue(name: String, value: Any?) {
    val exists = this.attributes
        .firstOrNull { it.attributeName == name }
    if (exists == null) {
        this.createAttribute(name)
    }
    val valueList = this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?: emptyList()
    if (valueList.isEmpty()) {
        this.attributes
            .firstOrNull { it.attributeName == name }
            ?.value = valueList + ObjectAttributeValue(
            value,
            "",
            null
        )
    } else {
        valueList
            .firstOrNull()
            ?.value = value
    }
}

fun MyInsightEntry.setStringValue(name: String, value: String) {
    this.setValue(name, value)
}

fun MyInsightEntry.getIntValue(name: String): Int? =
    getStringValue(name)
        ?.toInt()

fun MyInsightEntry.setIntValue(name: String, value: Int?) {
    this.setValue(name, value)
}

fun MyInsightEntry.getFloatValue(name: String): Float? =
    getStringValue(name)
        ?.toFloat()

fun MyInsightEntry.setFloatValue(name: String, value: Float?) {
    this.setValue(name, value)
}

fun MyInsightEntry.getBooleanValue(name: String): Boolean? =
    getStringValue(name)
        ?.toBoolean()

fun MyInsightEntry.setBooleanValue(name: String, value: Boolean?) {
    this.setValue(name, value)
}

fun MyInsightEntry.getDateTimeValue(name: String): DateTime? =
    getStringValue(name)
        ?.let { DateTime.parse(it) }

fun MyInsightEntry.setDateTimeValue(name: String, value: DateTime?) {
    this.setValue(name, value.toString())
}

fun MyInsightEntry.getSingleReference(name: String): MyInsightReference? =
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?.firstOrNull()
        ?.referencedObject
        ?.let {
            MyInsightReference(
                it.objectType!!.id,
                it.objectType!!.name,
                it.id,
                it.objectKey,
                it.label
            )
        }


fun MyInsightEntry.removeReference(name: String, objId: Int) {
    val objReferences = this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?.mapNotNull { it.referencedObject }
        ?: emptyList()
    val resultReferences = objReferences
        .filter { it.id != objId }
        .map {
            ObjectAttributeValue(
                null,
                null,
                it
            )
        }
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value = resultReferences
}

fun MyInsightEntry.clearReferences(name: String) {
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value = emptyList()
}

fun MyInsightEntry.addReference(name: String, objId: Int) {
    val references = this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?: emptyList()
    val added = ObjectAttributeValue(
        null,
        null,
        ReferencedObject(
            objId,
            "",
            "",
            null
        )

    )
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value = (references + added)
}

fun MyInsightEntry.setSingleReference(name: String, objId: Int) {
    this.clearReferences(name)
    this.addReference(name, objId)
}

fun MyInsightEntry.getMultiReference(name: String): List<MyInsightReference> =
    this.attributes
        .firstOrNull { it.attributeName == name }
        ?.value
        ?.mapNotNull { it.referencedObject }
        ?.map { reference ->
            MyInsightReference(
                reference.objectType!!.id,
                reference.objectType!!.name,
                reference.id,
                reference.objectKey,
                reference.label
            )
        }
        ?: emptyList()

data class MyInsightReference(
    val objectTypeId: Int,
    val objectTypeName: String,
    val objectId: Int,
    val objectKey: String,
    val objectName: String
)

data class MyInsightAttribute(
    val attributeId: Int,
    val attributeName: String,
    var value: List<ObjectAttributeValue>
)

data class ObjectTypeSchema(
    val id: Int,
    val name: String,
    var attributes: List<ObjectTypeSchemaAttribute>
)

data class ObjectEditItem(
    val objectTypeId: Int,
    val attributes: List<ObjectEditItemAttribute>
)

data class ObjectEditItemAttribute(
    val objectTypeAttributeId: Int,
    val objectAttributeValues: List<ObjectEditItemAttributeValue>
)

data class ObjectEditItemAttributeValue(
    val value: Any?
)

data class ObjectTypeSchemaAttribute(
    val id: Int,
    val name: String,
    val defaultType: ObjectTypeAttributeDefaultType?,
    val options: String
)

data class ObjectTypeAttributeDefaultType(
    val id: Int,
    val name: String
)

data class InsightObjectEntries(
    val objectEntries: List<InsightObject>
)

data class InsightSchemas(
    val objectschemas: List<InsightSchema>
)

data class InsightSchema(
    val id: Int,
    val name: String
)

data class InsightObject(
    val id: Int,
    val label: String,
    val objectKey: String,
    val objectType: ObjectType,
    val attributes: List<InsightAttribute>
)

data class ObjectType(
    val id: Int,
    val name: String,
    val objectSchemaId: Int
)

data class InsightObjectAttribute(
    val id: Int,
    val objectTypeAttribute: List<InsightAttribute>
)

data class InsightAttribute(
    val id: Int,
    val objectTypeAttribute: ObjectTypeAttribute?,
    val objectTypeAttributeId: Int,
    val objectId: Int,
    val objectAttributeValues: List<ObjectAttributeValue>
)

data class ObjectTypeAttribute(
    val id: Int,
    val name: String,
    val referenceObjectTypeId: Int,
    val referenceObjectType: ObjectType
)

data class ObjectAttributeValue(
    var value: Any?,
    var displayValue: Any?,
    var referencedObject: ReferencedObject?
)

data class ReferencedObject(
    var id: Int,
    var label: String,
    var objectKey: String,
    var objectType: ReferencedObjectType?
)

data class ReferencedObjectType(
    val id: Int,
    val name: String
)

data class InsightHistoryItem(
    val id: Int,
    val affectedAttribute: String,
    val newValue: String,
    val actor: Actor,
    val type: Int,
    val created: String,
    val updated: String,
    val objectId: Int
)

data class Actor(
    val name: String
)

data class ObjectUpdateResponse(
    val id: Int,
    val objectKey: String
)


data class InsightAttachment(
    val id: Int,
    val author: String,
    val mimeType: String,
    val filename: String,
    val filesize: String,
    val created: String,
    val comment: String,
    val commentOutput: String,
    val url: String
) {
    suspend fun getBytes(): ByteArray {
        return AttachmentOperator.downloadAttachment(this)
    }

    suspend fun delete(): Boolean {
        if (id <= 0) {
            return false
        }
        AttachmentOperator.deleteAttachment(this.id)
        return true
    }
}