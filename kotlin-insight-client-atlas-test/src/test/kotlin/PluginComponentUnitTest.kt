package com.linkedplanet.plugin.confluence.insightclient.test

import org.junit.Test
import com.linkedplanet.plugin.confluence.insightclient.test.api.PluginComponent

import org.junit.Assert.assertEquals

class PluginComponentUnitTest {

    @Test
    fun testMyName() {
        assertEquals("kotlin-insight-client-atlas-test", PluginComponent.name)
    }

}
