package com.linkedplanet.kotlininsightwrapper

import com.linkedplanet.kotlininsightwrapper.core.*
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test


abstract class AbstractMainTest {

    @Test
    fun testObjectsPaginationSize() {
        println("### START testObjectsPaginationSize")
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
        println("### END testObjectsPaginationSize")
    }

    @Test
    fun testIQLPaginationSize() {
        println("### START testIQLPaginationSize")
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
        println("### END testIQLPaginationSize")
    }

    @Test
    fun testObjectsWithPaginationAllInOne() {
        println("### START testObjectsWithPaginationAllInOne")
        val many = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.Many.name)
        }
        assertTrue(many.size == 55)
        println("### END testObjectsWithPaginationAllInOne")
    }

    @Test
    fun testIQLWithPaginationAllInOne() {
        println("### START testIQLWithPaginationAllInOne")
        val many = runBlocking {
            ObjectOperator.getObjectsByIQL(1, OBJECTS.Many.name, "Name is not empty")
        }
        assertTrue(many.size == 55)
        println("### END testIQLWithPaginationAllInOne")
    }

    @Test
    fun testObjectsWithPaginationPages() {
        println("### START testObjectsWithPaginationPages")
        val manyPage1 = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.Many.name, 1)
        }
        val valuesPageOne = manyPage1.map { it.getStringValue("Name")!!.toInt() }
        listOf(1,10,11,12,13,14,15,16,17,18,19,2,20,21,22,23,24,25,26,27,28,29,3,30,31).forEach {
            assertTrue(valuesPageOne.contains(it))
        }
        assertTrue(manyPage1.size == 25)

        val manyPage2 = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.Many.name, 2, 26)
        }
        val valuesPage2 = manyPage2.map { it.getStringValue("Name")!!.toInt() }
        listOf(33,34,35,36,37,38,39,4,40,41,42,43,44,45,46,47,48,49,5,50,51,52,53,54,55, 6).forEach {
            assertTrue(valuesPage2.contains(it))
        }
        assertTrue(manyPage2.size == 26)
        println("### END testObjectsWithPaginationPages")
    }

    @Test
    fun testIQLWithPaginationPages() {
        println("### START testIQLWithPaginationPages")
        val manyPage1 = runBlocking {
            ObjectOperator.getObjectsByIQL(1, OBJECTS.Many.name, "Name is not empty", 1)
        }
        val valuesPageOne = manyPage1.map { it.getStringValue("Name")!!.toInt() }
        listOf(1,10,11,12,13,14,15,16,17,18,19,2,20,21,22,23,24,25,26,27,28,29,3,30,31).forEach {
            assertTrue(valuesPageOne.contains(it))
        }
        assertTrue(manyPage1.size == 25)

        val manyPage2 = runBlocking {
            ObjectOperator.getObjectsByIQL(1, OBJECTS.Many.name, "Name is not empty", 2, 26)
        }
        val valuesPage2 = manyPage2.map { it.getStringValue("Name")!!.toInt() }
        listOf(33,34,35,36,37,38,39,4,40,41,42,43,44,45,46,47,48,49,5,50,51,52,53,54,55, 6).forEach {
            assertTrue(valuesPage2.contains(it))
        }
        assertTrue(manyPage2.size == 26)
        println("### END testIQLWithPaginationPages")
    }

    @Test
    fun testObjectListWithFlatReference() {
        println("### START testObjectListWithFlatReference")
        val companies = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.Company.name)
        }
        assertTrue(companies.size == 1)
        val company = companies.first()
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(company.getSingleReference(COMPANY.Country.name)!!.objectName == "Germany")
        println("### END testObjectListWithFlatReference")
    }

    @Test
    fun testObjectListWithResolvedReference() {
        println("### START testObjectListWithResolvedReference")
        val companies = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.Company.name)
        }
        assertTrue(companies.size == 1)
        val company = companies.first()
        val country = runBlocking {
            ObjectOperator.getObject(
                1,
                OBJECTS.Country.name,
                company.getSingleReference(COMPANY.Country.name)!!.objectId
            )!!
        }
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(country.getStringValue(COUNTRY.Name.name) == "Germany")
        assertTrue(country.getStringValue(COUNTRY.ShortName.name) == "DE")
        println("### END testObjectListWithResolvedReference")
    }

    @Test
    fun testObjectById() {
        println("### START testObjectById")
        val company = runBlocking {
            ObjectOperator.getObject(1, OBJECTS.Company.name, 1)!!
        }
        assertTrue(company.id == 1)
        assertTrue(company.getStringValue(COMPANY.Name.name) == "Test GmbH")
        assertTrue(company.getSingleReference(COMPANY.Country.name)!!.objectName == "Germany")
        println("### END testObjectById")
    }

    @Test
    fun testObjectWithListAttributes() {
        println("### START testObjectWithListAttributes")
        val obj = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.TestWithLists.name)
        }.first()

        val references = obj.getMultiReference(TEST_WITH_LISTS.ItemList.name)
        val idList = references.map { it.objectId }
        val nameList = references.map { it.objectName }
        val refList = references.map { insightReference ->
            runBlocking {
                ObjectOperator.getObject(1, OBJECTS.SimpleObject.name, insightReference.objectId)!!
            }
        }
        val firstNameList = refList.map { it.getStringValue(SIMPLE_OBJECT.Firstname.name) }

        assertTrue(idList == listOf(35, 36, 37))
        assertTrue(nameList == listOf("Object1", "Object2", "Object3"))
        assertTrue(firstNameList == listOf("F1", "F2", "F3"))
        println("### END testObjectWithListAttributes")
    }

    @Test
    fun testAddingSelectList(){
        println("### START testAddingSelectList")
        val obj = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.TestWithLists.name)
        }.first()
        val results = obj.getValueList("StringList")
        assertTrue(results.isEmpty())
        obj.addValue("StringList", "A")
        obj.addValue("StringList", "B")
        runBlocking { ObjectOperator.updateObject(1, obj) }

        val obj2 = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.TestWithLists.name)
        }.first()
        val results2 = obj2.getValueList("StringList")
        assertTrue(results2.size == 2)
        assertTrue(results2.contains("A"))
        assertTrue(results2.contains("B"))
        obj2.removeValue("StringList", "B")
        runBlocking { ObjectOperator.updateObject(1, obj2) }

        val obj3 = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.TestWithLists.name)
        }.first()
        val results3 = obj3.getValueList("StringList")
        assertTrue(results3.size == 1)
        assertTrue(results3.contains("A"))
        obj3.removeValue("StringList", "A")
        runBlocking { ObjectOperator.updateObject(1, obj3) }

        val obj4 = runBlocking {
            ObjectOperator.getObjects(1, OBJECTS.TestWithLists.name)
        }.first()
        val results4 = obj4.getValueList("StringList")
        assertTrue(results4.isEmpty())
        println("### END testAddingSelectList")
    }

    @Test
    fun testSchemaLoad() {
        println("### START testSchemaLoad")
        val mySchemas = runBlocking {
            ObjectTypeOperator.loadAllObjectTypeSchemas()
        }
        val schemas = mySchemas
        println("### END testSchemaLoad")
    }

    @Test
    fun testCreateAndDelete() {
        println("### START testCreateAndDelete")
        runBlocking {
            // Check England does not exist
            val countryBeforeCreate = ObjectOperator.getObjectByName(1, OBJECTS.Company.name, "England")
            val companyBeforeCreate = ObjectOperator.getObjectByName(1, OBJECTS.Company.name, "MyTestCompany GmbH")
            assertTrue(countryBeforeCreate == null)
            assertTrue(companyBeforeCreate == null)

            // Create and check direct result
            var country1 = ObjectOperator.createEmptyObject(1, OBJECTS.Country.name)
            country1.setStringValue(COUNTRY.Name.name, "England")
            country1.setStringValue(COUNTRY.ShortName.name, "GB")
            country1 = ObjectOperator.createObject(1, country1)

            var company1 = ObjectOperator.createEmptyObject(1, OBJECTS.Company.name)
            company1.setStringValue(COMPANY.Name.name, "MyTestCompany GmbH")
            company1.setSingleReference(COMPANY.Country.name, country1.id)
            company1 = ObjectOperator.createObject(1, company1)

            assertTrue(country1.id > 0)
            assertTrue(country1.getStringValue(COUNTRY.Key.name)!!.isNotBlank())
            assertTrue(company1.getSingleReference(COMPANY.Country.name)!!.objectId > 0)
            assertTrue(company1.getSingleReference(COMPANY.Country.name)!!.objectKey.isNotBlank())

            // Check England does exists
            val countryReference = company1.getSingleReference(COMPANY.Country.name)!!
            val countryAfterCreate = ObjectOperator.getObjectByName(1, OBJECTS.Country.name, "England")!!
            val companyAfterCreate = ObjectOperator.getObjectByName(1, OBJECTS.Company.name, "MyTestCompany GmbH")!!
            assertTrue(countryAfterCreate.id == countryReference.objectId)
            assertTrue(countryAfterCreate.getStringValue(COUNTRY.Key.name) == countryReference.objectKey)
            assertTrue(countryAfterCreate.getStringValue(COUNTRY.Name.name) == countryReference.objectName)
            assertTrue(companyAfterCreate.id == company1.id)

            // Check Delete
            ObjectOperator.deleteObject(countryReference.objectId)
            ObjectOperator.deleteObject(company1.id)
            val companyAfterDelete = ObjectOperator.getObjectByName(1, OBJECTS.Company.name, company1.getStringValue(
                COMPANY.Name.name)!!)
            val countryAfterDelete = ObjectOperator.getObjectByName(1, OBJECTS.Country.name, company1.getStringValue(
                COUNTRY.Name.name)!!)
            assertTrue(companyAfterDelete == null)
            assertTrue(countryAfterDelete == null)
        }
        println("### END testCreateAndDelete")
    }

    @Test
    fun testFilter() {
        println("### START testFilter")
        runBlocking {
            val countries = ObjectOperator.getObjectsByIQL(1, OBJECTS.Country.name, "\"ShortName\"=\"DE\"")!!
            assertTrue(countries.size == 1)
            assertTrue(countries.first().getStringValue(COUNTRY.ShortName.name) == "DE")
            assertTrue(countries.first().getStringValue(COUNTRY.Name.name) == "Germany")
        }
        println("### END testFilter")
    }

    @Test
    fun testUpdate() {
        println("### START testUpdate")
        runBlocking {
            var country = ObjectOperator.getObjectByName(1, OBJECTS.Country.name, "Germany")!!
            assertTrue(country.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(country.getStringValue(COUNTRY.ShortName.name) == "DE")
            country.setStringValue(COUNTRY.ShortName.name, "ED")
            country = ObjectOperator.updateObject(1, country)

            val country2 = ObjectOperator.getObjectByName(1, OBJECTS.Country.name, "Germany")!!
            assertTrue(country2.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(country2.getStringValue(COUNTRY.ShortName.name) == "ED")

            var countryAfterUpdate = ObjectOperator.getObjectByName(1, OBJECTS.Country.name, "Germany")!!
            assertTrue(countryAfterUpdate.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(countryAfterUpdate.getStringValue(COUNTRY.ShortName.name) == "ED")
            countryAfterUpdate.setStringValue(COUNTRY.ShortName.name, "DE")
            countryAfterUpdate = ObjectOperator.updateObject(1, countryAfterUpdate)

            val countryAfterReUpdate = ObjectOperator.getObjectByName(1, OBJECTS.Country.name, "Germany")!!
            assertTrue(countryAfterReUpdate.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(countryAfterReUpdate.getStringValue(COUNTRY.ShortName.name) == "DE")
        }
        println("### END testUpdate")
    }

    @Test
    fun testHistory() {
        println("### START testHistory")
        runBlocking {
            val country = ObjectOperator.getObjectByName(1, OBJECTS.Country.name, "Germany")!!
            val historyItems = HistoryOperator.getHistory(country.id)
            assertTrue(historyItems.isNotEmpty())
        }
        println("### END testHistory")
    }

    /*
    @Test
    fun testAttachments() {
        println("### START testAttachments")
        runBlocking {
            val country = ObjectOperator.getObjectByName(1, OBJECTS.Country.name, "Germany")!!
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
        println("### END testAttachments")
    }
     */
}