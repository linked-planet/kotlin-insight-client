import com.linkedplanet.kotlininsightwrapper.core.*
import com.linkedplanet.kotlininsightwrapper.ktor.*
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
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
        assertTrue(companies.size == 1)
        val company = companies.first()
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(company.getSingleReference(COMPANY.Country.name)!!.objectName == "Germany")
    }

    fun testObjectListWithResolvedReference() {
        val companies = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.Company.name)
        }
        assertTrue(companies.size == 1)
        val company = companies.first()
        val country = runBlocking {
            KtorObjectOperator.getObject(
                OBJECTS.Country.name,
                company.getSingleReference(COMPANY.Country.name)!!.objectId
            )!!
        }
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(country.getStringValue(COUNTRY.Name.name) == "Germany")
        assertTrue(country.getStringValue(COUNTRY.ShortName.name) == "DE")
    }

    fun testObjectById() {
        val company = runBlocking {
            KtorObjectOperator.getObject(OBJECTS.Company.name, 1)!!
        }
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(company.getSingleReference(COMPANY.Country.name)!!.objectName == "Germany")
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

        assertTrue(idList == listOf(35, 36, 37))
        assertTrue(nameList == listOf("Object1", "Object2", "Object3"))
        assertTrue(firstNameList == listOf("F1", "F2", "F3"))
        println("")
    }

    @Test
    fun testAddingSelectList() {
        val obj = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results = obj.getValueList("StringList")
        assertTrue(results.isEmpty())
        obj.addValue("StringList", "A")
        obj.addValue("StringList", "B")
        runBlocking { KtorObjectOperator.updateObject(obj) }

        val obj2 = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results2 = obj2.getValueList("StringList")
        assertTrue(results2.size == 2)
        assertTrue(results2.contains("A"))
        assertTrue(results2.contains("B"))
        obj2.removeValue("StringList", "B")
        runBlocking { KtorObjectOperator.updateObject(obj2) }

        val obj3 = runBlocking {
            KtorObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results3 = obj3.getValueList("StringList")
        assertTrue(results3.size == 1)
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
        val schemas = mySchemas
    }

    fun testCreateAndDelete() {
        runBlocking {
            // Check England does not exist
            val countryBeforeCreate = KtorObjectOperator.getObjectByName(OBJECTS.Company.name, "England")
            val companyBeforeCreate = KtorObjectOperator.getObjectByName(OBJECTS.Company.name, "MyTestCompany GmbH")
            assertTrue(countryBeforeCreate == null)
            assertTrue(companyBeforeCreate == null)

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
            assertTrue(countryAfterCreate.id == countryReference.objectId)
            assertTrue(countryAfterCreate.getStringValue(COUNTRY.Key.name) == countryReference.objectKey)
            assertTrue(countryAfterCreate.getStringValue(COUNTRY.Name.name) == countryReference.objectName)
            assertTrue(companyAfterCreate.id == company1.id)

            // Check Delete
            KtorObjectOperator.deleteObject(countryReference.objectId)
            KtorObjectOperator.deleteObject(company1.id)
            val companyAfterDelete =
                KtorObjectOperator.getObjectByName(OBJECTS.Company.name, company1.getStringValue(COMPANY.Name.name)!!)
            val countryAfterDelete =
                KtorObjectOperator.getObjectByName(OBJECTS.Country.name, company1.getStringValue(COUNTRY.Name.name)!!)
            assertTrue(companyAfterDelete == null)
            assertTrue(countryAfterDelete == null)
        }
    }

    fun testFilter() {
        runBlocking {
            val countries = KtorObjectOperator.getObjectsByIQL(OBJECTS.Country.name, "\"ShortName\"=\"DE\"")!!
            assertTrue(countries.size == 1)
            assertTrue(countries.first().getStringValue(COUNTRY.ShortName.name) == "DE")
            assertTrue(countries.first().getStringValue(COUNTRY.Name.name) == "Germany")
        }
    }

    fun testUpdate() {
        runBlocking {
            var country = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertTrue(country.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(country.getStringValue(COUNTRY.ShortName.name) == "DE")
            country.setStringValue(COUNTRY.ShortName.name, "ED")
            country = KtorObjectOperator.updateObject(country)

            val country2 = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertTrue(country2.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(country2.getStringValue(COUNTRY.ShortName.name) == "ED")

            var countryAfterUpdate = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertTrue(countryAfterUpdate.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(countryAfterUpdate.getStringValue(COUNTRY.ShortName.name) == "ED")
            countryAfterUpdate.setStringValue(COUNTRY.ShortName.name, "DE")
            countryAfterUpdate = KtorObjectOperator.updateObject(countryAfterUpdate)

            val countryAfterReUpdate = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertTrue(countryAfterReUpdate.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(countryAfterReUpdate.getStringValue(COUNTRY.ShortName.name) == "DE")
        }
    }

    fun testHistory() {
        runBlocking {
            val country = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            val historyItems = HistoryOperator.getHistory(country.id)
            assertTrue(historyItems.isNotEmpty())
        }
    }

    fun testAttachments() {
        runBlocking {
            val country = KtorObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            val uploadFile = File(MainTest::class.java.getResource("TestAttachment.pdf").file)
            val newAttachment =
                KtorAttachmentOperator.uploadAttachment(country.id, uploadFile.name, uploadFile.readBytes(), "MyComment")
            val attachments = KtorAttachmentOperator.getAttachments(country.id)
            assertTrue(attachments.size == 1)
            assertTrue(newAttachment.first().author == attachments.first().author)
            assertTrue(newAttachment.first().comment == attachments.first().comment)
            assertTrue(newAttachment.first().filename == attachments.first().filename)
            assertTrue(newAttachment.first().filesize == attachments.first().filesize)

            val downloadContent = attachments.first().getBytes()
            val md5Hash =
                MessageDigest.getInstance("MD5").digest(downloadContent).joinToString("") { "%02x".format(it) }
            assertTrue(md5Hash == "3c2f34b03f483bee145a442a4574ca26")

            val deleted = newAttachment.first().delete()
            val attachmentsAfterDelete = KtorAttachmentOperator.getAttachments(country.id)
            assertTrue(attachmentsAfterDelete.isEmpty())
        }
    }
}