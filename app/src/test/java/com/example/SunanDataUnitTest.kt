package com.example

import com.example.data.SunnahDefaultData
import com.example.model.AuthenticityGrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SunanDataUnitTest {
    @Test
    fun testDefaultSunanIntegrity() {
        val list = SunnahDefaultData.initialSunan
        assertFalse("Sunan list should not be empty", list.isEmpty())
        assertTrue("Sunan list should contain rich collection", list.size >= 25)

        for (item in list) {
            assertTrue("Title should not be blank", item.title.isNotBlank())
            assertTrue("Practical idea should not be blank", item.practicalIdea.isNotBlank())
            assertTrue("Evidence should not be blank", item.evidenceText.isNotBlank())
            assertTrue("Source should not be blank", item.source.isNotBlank())
            assertTrue("Narrator should not be blank", item.narrator.isNotBlank())
            assertNotNull("Authenticity must be valid", AuthenticityGrade.valueOf(item.authenticity))
        }
    }

    @Test
    fun testDomainConversion() {
        val sample = SunnahDefaultData.initialSunan.first()
        val domain = sample.toDomain(todayDate = "2026-11-05")
        assertEquals(sample.id, domain.id)
        assertEquals(sample.title, domain.title)
        assertFalse(domain.isAppliedToday)
    }
}
