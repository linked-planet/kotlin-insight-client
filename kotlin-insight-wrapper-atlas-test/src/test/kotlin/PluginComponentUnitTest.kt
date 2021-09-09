package com.linkedplanet.plugin.confluence.insightwrapper.test

import org.junit.Test
import com.linkedplanet.plugin.confluence.insightwrapper.test.api.PluginComponent

import org.junit.Assert.assertEquals

class PluginComponentUnitTest {

    @Test
    fun testMyName() {
        assertEquals("kotlin-insight-wrapper-atlas-test", PluginComponent.name)
    }

}
