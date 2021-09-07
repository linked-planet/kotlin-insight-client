import com.linkedplanet.kotlinInsightWrapper.*
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
        SimpleObject,
        Many
    }

    enum class MANY {
        Name
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

    @Test
    fun testObjectsPaginationSize() {
        val manySize = runBlocking {
            ObjectOperator.getObjectPages(OBJECTS.Many.name)
        }
        assertTrue(manySize == 3)

        val manySize2 = runBlocking {
            ObjectOperator.getObjectPages(OBJECTS.Many.name, 50)
        }
        assertTrue(manySize2 == 2)

        val manySize3 = runBlocking {
            ObjectOperator.getObjectPages(OBJECTS.Many.name, 100)
        }
        assertTrue(manySize3 == 1)
    }

    @Test
    fun testIQLPaginationSize() {
        val manySize = runBlocking {
            ObjectOperator.getObjectIqlPages(OBJECTS.Many.name, "Name is not empty")
        }
        assertTrue(manySize == 3)

        val manySize2 = runBlocking {
            ObjectOperator.getObjectIqlPages(OBJECTS.Many.name, "Name is not empty", 50)
        }
        assertTrue(manySize2 == 2)

        val manySize3 = runBlocking {
            ObjectOperator.getObjectIqlPages(OBJECTS.Many.name, "Name is not empty", 100)
        }
        assertTrue(manySize3 == 1)
    }

    @Test
    fun testObjectsWithPaginationAllInOne() {
        val many = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Many.name)
        }
        assertTrue(many.size == 55)
    }

    @Test
    fun testIQLWithPaginationAllInOne() {
        val many = runBlocking {
            ObjectOperator.getObjectsByIQL(OBJECTS.Many.name, "Name is not empty")
        }
        assertTrue(many.size == 55)
    }

    @Test
    fun testObjectsWithPaginationPages() {
        val manyPage1 = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Many.name, 1)
        }
        val valuesPageOne = manyPage1.map { it.getStringValue("Name")!!.toInt() }
        listOf(1,10,11,12,13,14,15,16,17,18,19,2,20,21,22,23,24,25,26,27,28,29,3,30,31).forEach {
            assertTrue(valuesPageOne.contains(it))
        }
        assertTrue(manyPage1.size == 25)

        val manyPage2 = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Many.name, 2, 26)
        }
        val valuesPage2 = manyPage2.map { it.getStringValue("Name")!!.toInt() }
        listOf(33,34,35,36,37,38,39,4,40,41,42,43,44,45,46,47,48,49,5,50,51,52,53,54,55, 6).forEach {
            assertTrue(valuesPage2.contains(it))
        }
        assertTrue(manyPage2.size == 26)
    }

    @Test
    fun testIQLWithPaginationPages() {
        val manyPage1 = runBlocking {
            ObjectOperator.getObjectsByIQL(OBJECTS.Many.name, "Name is not empty", 1)
        }
        val valuesPageOne = manyPage1.map { it.getStringValue("Name")!!.toInt() }
        listOf(1,10,11,12,13,14,15,16,17,18,19,2,20,21,22,23,24,25,26,27,28,29,3,30,31).forEach {
            assertTrue(valuesPageOne.contains(it))
        }
        assertTrue(manyPage1.size == 25)

        val manyPage2 = runBlocking {
            ObjectOperator.getObjectsByIQL(OBJECTS.Many.name, "Name is not empty", 2, 26)
        }
        val valuesPage2 = manyPage2.map { it.getStringValue("Name")!!.toInt() }
        listOf(33,34,35,36,37,38,39,4,40,41,42,43,44,45,46,47,48,49,5,50,51,52,53,54,55, 6).forEach {
            assertTrue(valuesPage2.contains(it))
        }
        assertTrue(manyPage2.size == 26)
    }

    @Test
    fun testObjectListWithFlatReference() {
        val companies = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Company.name)
        }
        assertTrue(companies.size == 1)
        val company = companies.first()
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(company.getSingleReference(COMPANY.Country.name)!!.objectName == "Germany")
    }

    @Test
    fun testObjectListWithResolvedReference() {
        val companies = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Company.name)
        }
        assertTrue(companies.size == 1)
        val company = companies.first()
        val country = runBlocking {
            ObjectOperator.getObject(
                OBJECTS.Country.name,
                company.getSingleReference(COMPANY.Country.name)!!.objectId
            )!!
        }
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(country.getStringValue(COUNTRY.Name.name) == "Germany")
        assertTrue(country.getStringValue(COUNTRY.ShortName.name) == "DE")
    }

    @Test
    fun testObjectById() {
        val company = runBlocking {
            ObjectOperator.getObject(OBJECTS.Company.name, 1)!!
        }
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(company.getSingleReference(COMPANY.Country.name)!!.objectName == "Germany")
    }

    @Test
    fun testObjectWithListAttributes() {
        val obj = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()

        val references = obj.getMultiReference(TEST_WITH_LISTS.ItemList.name)
        val idList = references.map { it.objectId }
        val nameList = references.map { it.objectName }
        val refList = references.map { insightReference ->
            runBlocking {
                ObjectOperator.getObject(OBJECTS.SimpleObject.name, insightReference.objectId)!!
            }
        }
        val firstNameList = refList.map { it.getStringValue(SIMPLE_OBJECT.Firstname.name) }

        assertTrue(idList == listOf(35, 36, 37))
        assertTrue(nameList == listOf("Object1", "Object2", "Object3"))
        assertTrue(firstNameList == listOf("F1", "F2", "F3"))
        println("")
    }

    @Test
    fun testAddingSelectList(){
        val obj = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results = obj.getValueList("StringList")
        assertTrue(results.isEmpty())
        obj.addValue("StringList", "A")
        obj.addValue("StringList", "B")
        runBlocking { ObjectOperator.updateObject(obj) }

        val obj2 = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results2 = obj2.getValueList("StringList")
        assertTrue(results2.size == 2)
        assertTrue(results2.contains("A"))
        assertTrue(results2.contains("B"))
        obj2.removeValue("StringList", "B")
        runBlocking { ObjectOperator.updateObject(obj2) }

        val obj3 = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results3 = obj3.getValueList("StringList")
        assertTrue(results3.size == 1)
        assertTrue(results3.contains("A"))
        obj3.removeValue("StringList", "A")
        runBlocking { ObjectOperator.updateObject(obj3) }

        val obj4 = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.name)
        }.first()
        val results4 = obj4.getValueList("StringList")
        assertTrue(results4.isEmpty())
    }

    @Test
    fun testSchemaLoad() {
        val mySchemas = runBlocking {
            SchemaOperator.loadSchema()
        }
        val schemas = mySchemas
    }

    @Test
    fun testCreateAndDelete() {
        runBlocking {
            // Check England does not exist
            val countryBeforeCreate = ObjectOperator.getObjectByName(OBJECTS.Company.name, "England")
            val companyBeforeCreate = ObjectOperator.getObjectByName(OBJECTS.Company.name, "MyTestCompany GmbH")
            assertTrue(countryBeforeCreate == null)
            assertTrue(companyBeforeCreate == null)

            // Create and check direct result
            var country1 = ObjectOperator.createEmptyObject(OBJECTS.Country.name)
            country1.setStringValue(COUNTRY.Name.name, "England")
            country1.setStringValue(COUNTRY.ShortName.name, "GB")
            country1 = ObjectOperator.createObject(country1)

            var company1 = ObjectOperator.createEmptyObject(OBJECTS.Company.name)
            company1.setStringValue(COMPANY.Name.name, "MyTestCompany GmbH")
            company1.setSingleReference(COMPANY.Country.name, country1.id)
            company1 = ObjectOperator.createObject(company1)

            assertTrue(country1.id > 0)
            assertTrue(country1.getStringValue(COUNTRY.Key.name)!!.isNotBlank())
            assertTrue(company1.getSingleReference(COMPANY.Country.name)!!.objectId > 0)
            assertTrue(company1.getSingleReference(COMPANY.Country.name)!!.objectKey.isNotBlank())

            // Check England does exists
            val countryReference = company1.getSingleReference(COMPANY.Country.name)!!
            val countryAfterCreate = ObjectOperator.getObjectByName(OBJECTS.Country.name, "England")!!
            val companyAfterCreate = ObjectOperator.getObjectByName(OBJECTS.Company.name, "MyTestCompany GmbH")!!
            assertTrue(countryAfterCreate.id == countryReference.objectId)
            assertTrue(countryAfterCreate.getStringValue(COUNTRY.Key.name) == countryReference.objectKey)
            assertTrue(countryAfterCreate.getStringValue(COUNTRY.Name.name) == countryReference.objectName)
            assertTrue(companyAfterCreate.id == company1.id)

            // Check Delete
            ObjectOperator.deleteObject(countryReference.objectId)
            ObjectOperator.deleteObject(company1.id)
            val companyAfterDelete = ObjectOperator.getObjectByName(OBJECTS.Company.name, company1.getStringValue(COMPANY.Name.name)!!)
            val countryAfterDelete = ObjectOperator.getObjectByName(OBJECTS.Country.name, company1.getStringValue(COUNTRY.Name.name)!!)
            assertTrue(companyAfterDelete == null)
            assertTrue(countryAfterDelete == null)
        }
    }

    @Test
    fun testFilter() {
        runBlocking {
            val countries = ObjectOperator.getObjectsByIQL(OBJECTS.Country.name, "\"ShortName\"=\"DE\"")!!
            assertTrue(countries.size == 1)
            assertTrue(countries.first().getStringValue(COUNTRY.ShortName.name) == "DE")
            assertTrue(countries.first().getStringValue(COUNTRY.Name.name) == "Germany")
        }
    }

    @Test
    fun testUpdate() {
        runBlocking {
            var country = ObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertTrue(country.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(country.getStringValue(COUNTRY.ShortName.name) == "DE")
            country.setStringValue(COUNTRY.ShortName.name, "ED")
            country = ObjectOperator.updateObject(country)

            val country2 = ObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertTrue(country2.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(country2.getStringValue(COUNTRY.ShortName.name) == "ED")

            var countryAfterUpdate = ObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertTrue(countryAfterUpdate.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(countryAfterUpdate.getStringValue(COUNTRY.ShortName.name) == "ED")
            countryAfterUpdate.setStringValue(COUNTRY.ShortName.name, "DE")
            countryAfterUpdate = ObjectOperator.updateObject(countryAfterUpdate)

            val countryAfterReUpdate = ObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            assertTrue(countryAfterReUpdate.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(countryAfterReUpdate.getStringValue(COUNTRY.ShortName.name) == "DE")
        }
    }

    @Test
    fun testHistory() {
        runBlocking {
            val country = ObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            val historyItems = HistoryOperator.getHistory(country.id)
            assertTrue(historyItems.isNotEmpty())
        }
    }

    @Test
    fun testAttachments() {
        runBlocking {
            val country = ObjectOperator.getObjectByName(OBJECTS.Country.name, "Germany")!!
            val uploadFile = File(MainTest::class.java.getResource("TestAttachment.pdf").file)
            val newAttachment = AttachmentOperator.uploadAttachment(country.id, uploadFile.name, uploadFile.readBytes(), "MyComment")
            val attachments = AttachmentOperator.getAttachments(country.id)
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
            val attachmentsAfterDelete = AttachmentOperator.getAttachments(country.id)
            assertTrue(attachmentsAfterDelete.isEmpty())
        }
    }
}