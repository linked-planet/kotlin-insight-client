package com.linkedplanet.kotlininsightwrapper.core

abstract class BaseHistoryOperator(val baseUrl: String) {

    fun getHistoryEndpoint(objectId: Int) = "${baseUrl}/rest/insight/1.0/object/${objectId}/history"
}