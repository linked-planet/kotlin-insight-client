import com.linkedplanet.kotlinInsightWrapper.*
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.rules.ExpectedException


class SchemaLoadingTest {


    @Test
    fun testInitAll() {
        InsightConfig.init("http://localhost:8080", 1, "admin", "admin")
        assertTrue(InsightConfig.objectSchemas.size == 5)
        val companies = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Company.name)
        }
        assertTrue(companies.size == 1)
    }

    @Test
    fun testInitCompany() {
        InsightConfig.init("http://localhost:8080", 1, "admin", "admin", listOf(OBJECTS.Company.name))
        assertTrue(InsightConfig.objectSchemas.size == 1)
        assertTrue(InsightConfig.objectSchemas.first().name == OBJECTS.Company.name)
        val companies = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Company.name)
        }
        assertTrue(companies.size == 1)
    }

    @Test(expected = ObjectSchemaNameException::class)
    fun testGetObjectSchemaWithoutInit() {
        InsightConfig.init("http://localhost:8080", 1, "admin", "admin", listOf(OBJECTS.Company.name))
        ExpectedException.none().expect(ObjectSchemaNameException::class.java)
        val many = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Many.name)
        }
    }

    @Test
    fun testInitCompanyMany() {
        InsightConfig.init("http://localhost:8080", 1, "admin", "admin", listOf(OBJECTS.Company.name, OBJECTS.Many.name))
        val names = InsightConfig.objectSchemas.map { it.name }
        assertTrue(InsightConfig.objectSchemas.size == 2)
        assertTrue(names.contains(OBJECTS.Company.name))
        assertTrue(names.contains(OBJECTS.Many.name))
    }

}