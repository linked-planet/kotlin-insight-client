import com.linkedplanet.kotlininsightwrapper.AbstractMainTest
import com.linkedplanet.kotlininsightwrapper.api.http.InsightConfig
import com.linkedplanet.kotlininsightwrapper.core.InsightSchemaCacheOperator
import com.linkedplanet.kotlininsightwrapper.core.InsightSchemaOperator
import com.linkedplanet.kotlininsightwrapper.ktor.KtorHttpClient
import org.junit.BeforeClass


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