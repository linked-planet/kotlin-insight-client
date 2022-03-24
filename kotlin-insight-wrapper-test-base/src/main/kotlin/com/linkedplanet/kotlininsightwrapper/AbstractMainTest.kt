package com.linkedplanet.kotlininsightwrapper

import arrow.core.getOrHandle
import com.linkedplanet.kotlininsightwrapper.api.model.*
import com.linkedplanet.kotlininsightwrapper.core.AttachmentOperator
import com.linkedplanet.kotlininsightwrapper.core.HistoryOperator
import com.linkedplanet.kotlininsightwrapper.core.ObjectOperator
import com.linkedplanet.kotlininsightwrapper.core.ObjectTypeOperator
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.security.MessageDigest


abstract class AbstractMainTest {

    @Test
    fun testObjectListWithFlatReference() {
        println("### START testObjectListWithFlatReference")
        val companies = runBlocking {
            ObjectOperator.getObjects(OBJECTS.Company.id).orNull()!!
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
            ObjectOperator.getObjects(OBJECTS.Company.id).orNull()!!
        }
        assertTrue(companies.size == 1)
        val company = companies.first()
        val country = runBlocking {
            ObjectOperator.getObjectById(
                company.getSingleReference(COMPANY.Country.name)!!.objectId
            ).orNull()!!
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
            ObjectOperator.getObjectById(1).orNull()!!
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
            ObjectOperator.getObjects(OBJECTS.TestWithLists.id).orNull()
        }!!.first()

        val references = obj.getMultiReference(TEST_WITH_LISTS.ItemList.name)
        val idList = references.map { it.objectId }
        val nameList = references.map { it.objectName }
        val refList = references.map { insightReference ->
            runBlocking {
                ObjectOperator.getObjectById(insightReference.objectId).orNull()!!
            }
        }
        val firstNameList = refList.map { it.getStringValue(SIMPLE_OBJECT.Firstname.name) }

        assertTrue(idList == listOf(35, 36, 37))
        assertTrue(nameList == listOf("Object1", "Object2", "Object3"))
        assertTrue(firstNameList == listOf("F1", "F2", "F3"))
        println("### END testObjectWithListAttributes")
    }

    @Test
    fun testAddingSelectList() {
        println("### START testAddingSelectList")
        val obj = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.id).orNull()
        }!!.first()
        val results = obj.getValueList("StringList")
        assertTrue(results.isEmpty())
        obj.addValue("StringList", "A")
        obj.addValue("StringList", "B")
        runBlocking { ObjectOperator.updateObject(obj).orNull() }

        val obj2 = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.id).orNull()
        }!!.first()
        val results2 = obj2.getValueList("StringList")
        assertTrue(results2.size == 2)
        assertTrue(results2.contains("A"))
        assertTrue(results2.contains("B"))
        obj2.removeValue("StringList", "B")
        runBlocking { ObjectOperator.updateObject(obj2).orNull() }

        val obj3 = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.id).orNull()
        }!!.first()
        val results3 = obj3.getValueList("StringList")
        assertTrue(results3.size == 1)
        assertTrue(results3.contains("A"))
        obj3.removeValue("StringList", "A")
        runBlocking { ObjectOperator.updateObject(obj3).orNull() }

        val obj4 = runBlocking {
            ObjectOperator.getObjects(OBJECTS.TestWithLists.id).orNull()
        }!!.first()
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
            val countryBeforeCreate = ObjectOperator.getObjectByName(OBJECTS.Country.id, "England").orNull()
            val companyBeforeCreate = ObjectOperator.getObjectByName(OBJECTS.Company.id, "MyTestCompany GmbH").orNull()
            assertTrue(countryBeforeCreate == null)
            assertTrue(companyBeforeCreate == null)

            // Create and check direct result
            val country1 = ObjectOperator.createObject(OBJECTS.Country.id) {
                it.setStringValue(COUNTRY.Name.name, "England")
                it.setStringValue(COUNTRY.ShortName.name, "GB")
            }.orNull()!!

            val company1 = ObjectOperator.createObject(OBJECTS.Company.id) {
                it.setStringValue(COMPANY.Name.name, "MyTestCompany GmbH")
                it.setSingleReference(COMPANY.Country.name, country1.id)
            }.orNull()!!

            assertTrue(country1.id > 0)
            assertTrue(country1.getStringValue(COUNTRY.Key.name)!!.isNotBlank())
            assertTrue(company1.getSingleReference(COMPANY.Country.name)!!.objectId > 0)
            assertTrue(company1.getSingleReference(COMPANY.Country.name)!!.objectKey.isNotBlank())

            // Check England does exists
            val countryReference = company1.getSingleReference(COMPANY.Country.name)!!
            val countryAfterCreate = ObjectOperator.getObjectByName(OBJECTS.Country.id, "England").orNull()!!
            val companyAfterCreate = ObjectOperator.getObjectByName(OBJECTS.Company.id, "MyTestCompany GmbH").orNull()!!
            assertTrue(countryAfterCreate.id == countryReference.objectId)
            assertTrue(countryAfterCreate.getStringValue(COUNTRY.Key.name) == countryReference.objectKey)
            assertTrue(countryAfterCreate.getStringValue(COUNTRY.Name.name) == countryReference.objectName)
            assertTrue(companyAfterCreate.id == company1.id)

            // Check Delete
            ObjectOperator.deleteObject(countryReference.objectId)
            ObjectOperator.deleteObject(company1.id)
            val companyAfterDelete = ObjectOperator.getObjectByName(
                OBJECTS.Company.id, company1.getStringValue(
                    COMPANY.Name.name
                )!!
            ).orNull()
            val countryAfterDelete = ObjectOperator.getObjectByName(
                OBJECTS.Country.id, company1.getStringValue(
                    COUNTRY.Name.name
                )!!
            ).orNull()
            assertTrue(companyAfterDelete == null)
            assertTrue(countryAfterDelete == null)
        }
        println("### END testCreateAndDelete")
    }

    @Test
    fun testFilter() {
        println("### START testFilter")
        runBlocking {
            val countries = ObjectOperator.getObjectsByIQL(OBJECTS.Country.id, false, "\"ShortName\"=\"DE\"").orNull()!!
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
            var country = ObjectOperator.getObjectByName(OBJECTS.Country.id, "Germany").orNull()!!
            assertTrue(country.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(country.getStringValue(COUNTRY.ShortName.name) == "DE")
            country.setStringValue(COUNTRY.ShortName.name, "ED")
            country = runBlocking { ObjectOperator.updateObject(country).orNull()!! }

            val country2 = ObjectOperator.getObjectByName(OBJECTS.Country.id, "Germany").orNull()!!
            assertTrue(country2.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(country2.getStringValue(COUNTRY.ShortName.name) == "ED")

            var countryAfterUpdate = ObjectOperator.getObjectByName(OBJECTS.Country.id, "Germany").orNull()!!
            assertTrue(countryAfterUpdate.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(countryAfterUpdate.getStringValue(COUNTRY.ShortName.name) == "ED")
            countryAfterUpdate.setStringValue(COUNTRY.ShortName.name, "DE")
            countryAfterUpdate = runBlocking { ObjectOperator.updateObject(countryAfterUpdate).orNull()!! }

            val countryAfterReUpdate = ObjectOperator.getObjectByName(OBJECTS.Country.id, "Germany").orNull()!!
            assertTrue(countryAfterReUpdate.getStringValue(COUNTRY.Name.name) == "Germany")
            assertTrue(countryAfterReUpdate.getStringValue(COUNTRY.ShortName.name) == "DE")
        }
        println("### END testUpdate")
    }

    @Test
    fun testHistory() {
        println("### START testHistory")
        runBlocking {
            val country = ObjectOperator.getObjectByName(OBJECTS.Country.id, "Germany").orNull()!!
            val historyItems = HistoryOperator.getHistory(country.id).orNull()!!
            assertTrue(historyItems.isNotEmpty())
        }
        println("### END testHistory")
    }


    @Test
    fun testAttachments() {
        println("### START testAttachments")
        runBlocking {
            val country = ObjectOperator.getObjectByName(OBJECTS.Country.id, "Germany").orNull()!!
            val uploadFile = File(AbstractMainTest::class.java.classLoader.getResource("TestAttachment.pdf").file)
            val newAttachment =
                AttachmentOperator.uploadAttachment(country.id, uploadFile.name, uploadFile.readBytes(), "MyComment")
                    .orNull()!!
            val attachments = AttachmentOperator.getAttachments(country.id).orNull() ?: emptyList()
            assertTrue(attachments.size == 1)
            assertTrue(newAttachment.first().author == attachments.first().author)
            assertTrue(newAttachment.first().comment == attachments.first().comment)
            assertTrue(newAttachment.first().filename == attachments.first().filename)
            assertTrue(newAttachment.first().filesize == attachments.first().filesize)

            val downloadContent = AttachmentOperator.downloadAttachment(attachments.first().url).orNull()!!
            val md5Hash =
                MessageDigest.getInstance("MD5").digest(downloadContent).joinToString("") { "%02x".format(it) }
            assertTrue(md5Hash == "3c2f34b03f483bee145a442a4574ca26")

            val deleted = AttachmentOperator.deleteAttachment(newAttachment.first().id).orNull()!!
            val attachmentsAfterDelete = AttachmentOperator.getAttachments(country.id).getOrHandle {
                throw IllegalStateException("Attachments could not be get")
            }
            assertTrue(attachmentsAfterDelete.isEmpty())
        }
        println("### END testAttachments")
    }
}