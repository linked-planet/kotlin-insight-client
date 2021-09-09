package it

import com.atlassian.applinks.api.ApplicationLinkService
import com.atlassian.applinks.api.application.jira.JiraApplicationType
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import com.atlassian.confluence.user.UserAccessor
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner
import com.atlassian.sal.api.ApplicationProperties
import com.linkedplanet.kotlininsightwrapper.atlas.AtlasHttpClient
import com.linkedplanet.kotlininsightwrapper.core.InsightConfig
import com.linkedplanet.kotlininsightwrapper.core.ObjectOperator
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AtlassianPluginsTestRunner::class)
class MainWiredTest {

    @Inject
    constructor(
        @ComponentImport userAccessor: UserAccessor,
        @ComponentImport applicationProperties: ApplicationProperties,
        @ComponentImport applicationLinkService: ApplicationLinkService
    ) {
        println("### Starting MainWiredTest")
        val serviceUser = userAccessor.getUserByName("admin")
        AuthenticatedUserThreadLocal.asUser(serviceUser)
        val appLink = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType::class.java)
        val httpClient = AtlasHttpClient(
            appLink
        )
        InsightConfig.init("http://localhost:8080", 1, httpClient)
        println("### Starting MainWiredTest")
    }

    @Test
    fun testObjectsPaginationSize() {
        println("### testObjectsPaginationSize")
        val manySize = runBlocking {
            ObjectOperator.getObjectPages(OBJECTS.Many.name)
        }
        TestCase.assertTrue(manySize == 3)

        val manySize2 = runBlocking {
            ObjectOperator.getObjectPages(OBJECTS.Many.name, 50)
        }
        TestCase.assertTrue(manySize2 == 2)

        val manySize3 = runBlocking {
            ObjectOperator.getObjectPages(OBJECTS.Many.name, 100)
        }
        TestCase.assertTrue(manySize3 == 1)
        println("### END testObjectsPaginationSize")
    }

}