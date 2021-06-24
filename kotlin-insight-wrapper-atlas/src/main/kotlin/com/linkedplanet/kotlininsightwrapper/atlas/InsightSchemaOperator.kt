package com.linkedplanet.kotlininsightwrapper.atlas

import com.atlassian.sal.api.net.Request
import com.google.gson.reflect.TypeToken
import org.apache.http.entity.ContentType

object InsightSchemaOperator {

    fun getSchemas(): InsightSchemas =
        executeRest(
            InsightConfig.appLink!!,
            Request.MethodType.GET,
            "${InsightConfig.baseUrl}/rest/insight/1.0/objectschema/list",
            emptyMap(),
            null,
            ContentType.APPLICATION_JSON.toString(),
            object : TypeToken<InsightSchemas>() {}.type
        )!!

}