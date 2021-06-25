package com.linkedplanet.kotlininsightwrapper.atlas

import com.atlassian.applinks.api.ApplicationLink
import com.linkedplanet.kotlininsightwrapper.core.ObjectTypeSchema


object InsightConfig {

    var appLink: ApplicationLink? = null

    var baseUrl: String = ""
    var objectSchemas: List<ObjectTypeSchema> = emptyList()


    fun init(appLink: ApplicationLink) {
        InsightConfig.appLink = appLink
        InsightConfig.baseUrl = appLink.rpcUrl.toString()
        objectSchemas = ObjectTypeOperator.loadAllObjectTypeSchemas()
    }
}