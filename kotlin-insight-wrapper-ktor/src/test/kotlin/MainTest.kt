import com.linkedplanet.kotlininsightwrapper.AbstractMainTest
import com.linkedplanet.kotlininsightwrapper.api.InsightConfig
import com.linkedplanet.kotlininsightwrapper.core.InsightSchemaCacheOperator
import org.junit.BeforeClass
import com.linkedplanet.kotlinhttpclient.ktor.KtorHttpClient


class MainTest: AbstractMainTest() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            println("#### Starting setUp")
            val httpClient = KtorHttpClient(
                "http://localhost:8080",
                "admin",
                "admin"
            )
            InsightConfig.init("http://localhost:8080", httpClient, InsightSchemaCacheOperator)
        }
    }
}