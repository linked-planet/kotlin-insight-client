package it.com.linkedplanet.plugin.confluence.insightwrapper.test

import com.atlassian.applinks.api.ApplicationLinkService
import com.atlassian.applinks.api.application.jira.JiraApplicationType
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import com.atlassian.confluence.user.UserAccessor
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner
import com.atlassian.sal.api.ApplicationProperties
import com.linkedplanet.kotlininsightwrapper.atlas.AtlasHttpClient
import com.linkedplanet.kotlininsightwrapper.core.InsightConfig
import com.linkedplanet.kotlininsightwrapper.core.ObjectOperator
import it.OBJECTS
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AtlassianPluginsTestRunner::class)
class MainWiredTest {

    private lateinit var userAccessor: UserAccessor
    private lateinit var applicationProperties: ApplicationProperties
    private lateinit var applicationLinkService: ApplicationLinkService

    constructor(
        userAccessor: UserAccessor,
        applicationProperties: ApplicationProperties,
        applicationLinkService: ApplicationLinkService
    ) {
        this.userAccessor = userAccessor
        this.applicationProperties = applicationProperties
        this.applicationLinkService = applicationLinkService
        println("### Starting MainWiredTest")
        println("### AppLinkUrl: ${applicationLinkService.getPrimaryApplicationLink(JiraApplicationType::class.java).displayUrl}")
        val serviceUser = userAccessor.getUserByName("admin")
        AuthenticatedUserThreadLocal.asUser(serviceUser)
        val appLink = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType::class.java)
        val httpClient = AtlasHttpClient(
            appLink
        )
        InsightConfig.init("http://localhost:8080", 1, httpClient)
        println("### Starting MainWiredTest")
    }

    @Before
    fun initTest() {
        val serviceUser = userAccessor.getUserByName("admin")
        AuthenticatedUserThreadLocal.asUser(serviceUser)
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