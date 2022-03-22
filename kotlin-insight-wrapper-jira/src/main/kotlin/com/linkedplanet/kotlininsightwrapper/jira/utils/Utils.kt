package com.linkedplanet.kotlininsightwrapper.jira.utils

import com.atlassian.jira.component.ComponentAccessor
import com.linkedplanet.kotlininsightwrapper.api.ObjectTypeAttributeDefaultType
import com.linkedplanet.kotlininsightwrapper.api.ObjectTypeSchema
import com.linkedplanet.kotlininsightwrapper.api.ObjectTypeSchemaAttribute
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade

val Int.objectType: ObjectTypeSchema
    get() {
        val objectTypeFacade = getFacade<ObjectTypeFacade>()
        val typeBean = objectTypeFacade.loadObjectType(this)
        val attributes = typeBean.id.objectTypeSchemaAttributes
        return ObjectTypeSchema(typeBean.id, typeBean.name, attributes)
    }

val Int.objectTypeSchemaAttributes: List<ObjectTypeSchemaAttribute>
    get() =
        getFacade<ObjectTypeAttributeFacade>().findObjectTypeAttributeBeans(this).map { attributeBean ->
            val type = ObjectTypeAttributeDefaultType(
                attributeBean.type.typeId,
                attributeBean.typeValue
            )
            ObjectTypeSchemaAttribute(
                attributeBean.id,
                attributeBean.name,
                type,
                attributeBean.options
            )
        }


inline fun <reified T> getFacade(): T {
    val facadeClass: Class<T> =
        ComponentAccessor.getPluginAccessor().classLoader.loadClass(T::class.java.canonicalName) as Class<T>
    val facade: T = ComponentAccessor.getOSGiComponentInstanceOfType(facadeClass)
    return facade
}
