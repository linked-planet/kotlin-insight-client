package com.linkedplanet.kotlininsightwrapper.jira

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.interfaces.ObjectOperatorInterface
import com.linkedplanet.kotlininsightwrapper.core.*
import com.atlassian.jira.component.ComponentAccessor
import com.linkedplanet.kotlininsightwrapper.api.*
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade
import com.riadalabs.jira.plugins.insight.services.model.ObjectResultBean

object ObjectOperator: ObjectOperatorInterface {
    override var RESULTS_PER_PAGE: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun getObjects(
        objectTypeId: Int,
        withChildren: Boolean,
        pageFrom: Int,
        pageTo: Int?,
        perPage: Int
    ): Either<DomainError, List<MyInsightEntry>> {
        TODO("Not yet implemented")
    }

    override suspend fun getObjectById(id: Int): Either<DomainError, MyInsightEntry?> {
        TODO("Not yet implemented")
    }

    override suspend fun getObjectByKey(key: String): Either<DomainError, MyInsightEntry?> {
        TODO("Not yet implemented")
    }

    override suspend fun getObjectByName(objectTypeId: Int, name: String): Either<DomainError, MyInsightEntry?> {
        TODO("Not yet implemented")
    }

    override suspend fun getObjectsByIQL(
        objectTypeId: Int,
        withChildren: Boolean,
        iql: String,
        pageFrom: Int,
        pageTo: Int?,
        perPage: Int
    ): Either<DomainError, List<MyInsightEntry>> {
//        val iqlFacadeClass = com.atlassian.jira.component.ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade")
//        val iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass)
//        val objects = iqlFacade.findObjectsByIQLAndSchema(objectTypeId, "")
        val limit = pageTo?.let { it*perPage }?:Int.MAX_VALUE
        val iqlFacadeClass: Class<IQLFacade> = ComponentAccessor.getPluginAccessor().classLoader.loadClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade") as Class<IQLFacade>
        val iqlFacade: IQLFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass)
        val objects: ObjectResultBean = iqlFacade.findObjects(iql, pageFrom*perPage, limit)

        objects.objects.map { objectBean ->

        }

        TODO("Not yet implemented")
    }

    override suspend fun updateObject(obj: MyInsightEntry): Either<DomainError, MyInsightEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteObject(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun createObject(
        objectTypeId: Int,
        func: (MyInsightEntry) -> Unit
    ): Either<DomainError, MyInsightEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getObjectPages(objectTypeId: Int, resultsPerPage: Int): Either<DomainError, Int> {
        TODO("Not yet implemented")
    }

    override suspend fun getObjectsByPlainIQL(iql: String): Either<DomainError, List<MyInsightEntry>> {
        TODO("Not yet implemented")
    }

    override fun InsightObject.toValue(): MyInsightEntry {
        TODO("Not yet implemented")
    }

    override fun InsightObjectEntries.toValues(): List<MyInsightEntry> {
        TODO("Not yet implemented")
    }

}