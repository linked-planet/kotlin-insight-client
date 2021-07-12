import com.linkedplanet.kotlininsightwrapper.core.*
import com.linkedplanet.kotlininsightwrapper.ktor.*
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import java.io.File
import java.security.MessageDigest

class MainTest : TestCase() {
    enum class OBJECTS {
        Company,
        Country,
        TestWithLists,
        SimpleObject
    }

    enum class COMPANY {
        Name,
        Country
    }

    enum class COUNTRY {
        Name,
        Key,
        ShortName
    }

    enum class TEST_WITH_LISTS {
        ItemList
    }

    enum class SIMPLE_OBJECT {
        Firstname,
    }

    override fun setUp() {
        super.setUp()
        println("#### Starting setUp")
        InsightConfig.init("http://localhost:8080", 1, "admin", "admin")
    }

    fun testObjectListWithFlatReference() {
        val companies = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.Company.name)
        }
        assertEquals(1, companies.size)
        val company = companies.first()
        assertEquals(1, company.id)
        assertEquals("Test GmbH", company.getStringValue(COMPANY.Name.name))
        assertEquals("Germany", company.getSingleReference(COMPANY.Country.name)!!.objectName)
    }

    fun testObjectListWithResolvedReference() {
        val companies = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.Company.name)
        }
        assertEquals(1, companies.size)
        val company = companies.first()
        val country = runBlocking {
            KtorObjectOperator.getObject(
                OBJECTS.Country.name,
                company.getSingleReference(COMPANY.Country.name)!!.objectId
            )!!
        }
        assertEquals(1, company.id)
        assertEquals("Test GmbH", company.getStringValue(COMPANY.Name.name))
        assertEquals("Germany", country.getStringValue(COUNTRY.Name.name))
        assertEquals("DE", country.getStringValue(COUNTRY.ShortName.name))
    }

    fun testObjectById() {
        val company = runBlocking {
            KtorObjectOperator.getObject(OBJECTS.Company.name, 1)!!
        }
        assertEquals(1, company.id)
        assertEquals("Test GmbH", company.getStringValue(COMPANY.Name.name))
        assertEquals("Germany", company.getSingleReference(COMPANY.Country.name)!!.objectName)
    }

    fun testObjectWithListAttributes() {
        val obj = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()

        val references = obj.getMultiReference(TEST_WITH_LISTS.ItemList.name)
        val idList = references.map { it.objectId }
        val nameList = references.map { it.objectName }
        val refList = references.map { insightReference ->
            runBlocking {
                KtorObjectOperator.getObject(OBJECTS.SimpleObject.name, insightReference.objectId)!!
            }
        }
        val firstNameList = refList.map { it.getStringValue(SIMPLE_OBJECT.Firstname.name) }

        assertEquals(listOf(35, 36, 37), idList)
        assertEquals(listOf("Object1", "Object2", "Object3"), nameList)
        assertEquals(listOf("F1", "F2", "F3"), firstNameList)
    }

    fun testAddingSelectList() {
        val obj1 = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results = obj1.getValueList("StringList")
        assertTrue(results.isEmpty())
        obj1.addValue("StringList", "A")
        obj1.addValue("StringList", "B")
        runBlocking { KtorObjectOperator.updateObject(obj1) }

        val obj2 = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results2 = obj2.getValueList("StringList")
        assertEquals(2, results2.size)
        assertTrue(results2.contains("A"))
        assertTrue(results2.contains("B"))
        obj2.removeValue("StringList", "B")
        runBlocking { KtorObjectOperator.updateObject(obj2) }

        val obj3 = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results3 = obj3.getValueList("StringList")
        assertEquals(1, results3.size)
        assertTrue(results3.contains("A"))
        obj3.removeValue("StringList", "A")
        runBlocking { KtorObjectOperator.updateObject(obj3) }

        val obj4 = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results4 = obj4.getValueList("StringList")
        assertTrue(results4.isEmpty())
    }

    fun testSchemaLoad() {
        val mySchemas = runBlocking {
            SchemaOperator.loadSchema()
        }
        assertNotNull(mySchemas)
        assertTrue(mySchemas.isNotEmpty())
    }

    fun testCreateAndDelete() {
        runBlocking {
            // Check England does not exist
            val countryBeforeCreate = KtorObjectOperator.getObjectByName(OBJECTS.Company.name, "England")
            val companyBeforeCreate = KtorObjectOperator.getObjectByName(OBJECTS.Company.name, "MyTestCompany GmbH")
            assertNull(countryBeforeCreate)
            assertNull(companyBeforeCreate)

            // Create and check direct result
            var country1 = KtorObjectOperator.createEmptyObject(OBJECTS.Country.name)
            country1.setStringValue(COUNTRY.Name.name, "England")
            country1.setStringValue(COUNTRY.ShortName.name, "GB")
            country1 = KtorObjectOperator.createObject(country1)

            var company1 = KtorObjectOperator.createEmptyObject(OBJECTS.Company.name)
            company1.setStringValue(COMPANY.Name.name, "MyTestCompany GmbH")
            company1.setSingleReference(COMPANY.Country.name, country1.id)
            company1 = KtorObjectOperator.createObject(company1)

            assertTrue(country1.id > 0)
            assertTrue(country1.getStringValue(COUNTRY.Key.name)!!.isNotBlank())
            assertTrue(company1.getSingleReference(COMPANY.Country.name)!!.objectId > 0)
            assertTrue(company1.getSingleReference(COMPANY.Country.name)!!.objectKey.isNotBlank())

            // Check England does exists
            val countryReference = company1.getSingleReference(COMPANY.Country.name)!!
            val countryAfterCreate = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "England")!!
            val companyAfterCreate = KtorObjectOperator.getObjectByName(OBJECTS.Company.name, "MyTestCompany GmbH")!!
            assertEquals(countryReference.objectId, countryAfterCreate.id)
            assertEquals(countryReference.objectKey, countryAfterCreate.getStringValue(COUNTRY.Key.name))
            assertEquals(countryReference.objectName, countryAfterCreate.getStringValue(COUNTRY.Name.name))
            assertEquals(company1.id, companyAfterCreate.id)

            // Check Delete
            KtorObjectOperator.deleteObject(countryReference.objectId)
            KtorObjectOperator.deleteObject(company1.id)
            val companyAfterDelete =
                KtorObjectOperator.getObjectByName(OBJECTS.Company.name, company1.getStringValue(COMPANY.Name.name)!!)
            val countryAfterDelete =
                KtorObjectOperator.getObjectByName(OBJECTS.Country.name, company1.getStringValue(COUNTRY.Name.name)!!)
            assertNull(companyAfterDelete)
            assertNull(countryAfterDelete)
        }
    }

    fun testFilter() {
        runBlocking {
            val countries = KtorObjectOperator.getObjectsByIQL(OBJECTS.Country.name, "\"ShortName\"=\"DE\"")
            assertEquals(1, countries.size)
            assertEquals("DE", countries.first().getStringValue(COUNTRY.ShortName.name))
            assertEquals("Germany", countries.first().getStringValue(COUNTRY.Name.name))
        }
    }

    fun testUpdate() {
        runBlocking {
            var country = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertEquals("Germany", country.getStringValue(COUNTRY.Name.name))
            assertEquals("DE", country.getStringValue(COUNTRY.ShortName.name))
            country.setStringValue(COUNTRY.ShortName.name, "ED")
            country = KtorObjectOperator.updateObject(country)
            assertEquals("Germany", country.getStringValue(COUNTRY.Name.name))
            assertEquals("ED", country.getStringValue(COUNTRY.ShortName.name))

            val country2 = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertEquals("Germany", country2.getStringValue(COUNTRY.Name.name))
            assertEquals("ED", country2.getStringValue(COUNTRY.ShortName.name))

            var countryAfterUpdate = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertEquals("Germany", countryAfterUpdate.getStringValue(COUNTRY.Name.name))
            assertEquals("ED", countryAfterUpdate.getStringValue(COUNTRY.ShortName.name))
            countryAfterUpdate.setStringValue(COUNTRY.ShortName.name, "DE")
            countryAfterUpdate = KtorObjectOperator.updateObject(countryAfterUpdate)
            assertEquals("Germany", countryAfterUpdate.getStringValue(COUNTRY.Name.name))
            assertEquals("DE", countryAfterUpdate.getStringValue(COUNTRY.ShortName.name))

            val countryAfterReUpdate = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertEquals("Germany", countryAfterReUpdate.getStringValue(COUNTRY.Name.name))
            assertEquals("DE", countryAfterReUpdate.getStringValue(COUNTRY.ShortName.name))
        }
    }

    fun testHistory() {
        runBlocking {
            val country = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            val historyItems = KtorHistoryOperator.getHistory(country.id)
            assertTrue(historyItems.isNotEmpty())
        }
    }

    fun testAttachments() {
        runBlocking {
            val country = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            val uploadFile = File(MainTest::class.java.getResource("TestAttachment.pdf").file)
            val newAttachment =
                KtorAttachmentOperator.uploadAttachment(
                    country.id,
                    uploadFile.name,
                    uploadFile.readBytes(),
                    "MyComment"
                )
            val attachments = KtorAttachmentOperator.getAttachments(country.id)
            assertEquals(1, attachments.size)
            assertEquals(attachments.first().author, newAttachment.first().author)
            assertEquals(attachments.first().comment, newAttachment.first().comment)
            assertEquals(attachments.first().filename, newAttachment.first().filename)
            assertEquals(attachments.first().filesize, newAttachment.first().filesize)

            val downloadContent = attachments.first().getBytes()
            val md5Hash =
                MessageDigest.getInstance("MD5").digest(downloadContent).joinToString("") { "%02x".format(it) }
            assertTrue(md5Hash == "3c2f34b03f483bee145a442a4574ca26")

            newAttachment.first().delete()
            val attachmentsAfterDelete = KtorAttachmentOperator.getAttachments(country.id)
            assertTrue(attachmentsAfterDelete.isEmpty())
        }
    }
}