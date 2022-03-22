package com.linkedplanet.kotlininsightwrapper.jira.utils

import com.linkedplanet.kotlininsightwrapper.api.MyInsightAttribute
import com.linkedplanet.kotlininsightwrapper.api.MyInsightEntry
import com.linkedplanet.kotlininsightwrapper.api.ObjectAttributeValue
import com.linkedplanet.kotlininsightwrapper.api.ReferencedObject
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean

object ObjectMapper {

    fun ObjectBean.toMyInsightEntry(): MyInsightEntry {
        val attributes: List<MyInsightAttribute> = this.objectAttributeBeans.map { attributeBean ->
            val values: List<ObjectAttributeValue> = attributeBean.objectAttributeValueBeans.map { attributeValueBean ->
                val referenceId = attributeValueBean.referencedObjectBeanId
                if (referenceId > 0) {
                    ObjectAttributeValue(
                        attributeValueBean.value,
                        attributeValueBean.textValue,
                        ReferencedObject(
                            referenceId,
                            attributeValueBean.textValue,
                            attributeValueBean.textValue,
                            null
                        )
                    )
                } else {
                    ObjectAttributeValue(
                        attributeValueBean.value,
                        attributeValueBean.textValue,
                        null
                    )
                }
            }
            MyInsightAttribute(
                attributeBean.id.toInt(),
                attributeBean.id.toString(),
                values
            )
        }
        return MyInsightEntry(
            this.objectTypeId,
            this.id,
            attributes
        )
    }
}