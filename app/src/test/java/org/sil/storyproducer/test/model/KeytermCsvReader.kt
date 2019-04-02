package org.sil.storyproducer.test.model

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sil.storyproducer.model.Keyterm
import org.sil.storyproducer.model.KeytermCsvReader
import java.io.InputStreamReader

@RunWith(RobolectricTestRunner::class)
class TestKeytermCsvReader {
    @Test
    fun readAll_When_AllFieldsFilled_Should_ReturnListWithOneKeytermWithAllThoseFields() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/AllFieldsFilled.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("disciples"), listOf("student"), "Some notes.", listOf("apostle")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_TermFieldEmptyAndOtherFieldsFilled_Should_ReturnEmptyList() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/TermFieldEmptyAndOtherFieldsFilled.csv")

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertTrue(actualKeyterms.isEmpty())
    }

    @Test
    fun readAll_When_OtherFormsFieldEmptyAndOtherFieldsFilled_Should_ReturnListWithOneKeytermWithAllThoseFields() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/OtherFormsFieldEmptyAndOtherFieldsFilled.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf("student"), "Some notes.", listOf("apostle")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_AlternateRenderingsFieldEmptyAndOtherFieldsFilled_Should_ReturnListWithOneKeytermWithAllThoseFields() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/AlternateRenderingsFieldEmptyAndOtherFieldsFilled.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("disciples"), listOf(), "Some notes.", listOf("apostle")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_NotesFieldEmptyAndOtherFieldsFilled_Should_ReturnListWithOneKeytermWithAllThoseFields() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/NotesFieldEmptyAndOtherFieldsFilled.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("disciples"), listOf("student"), "", listOf("apostle")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }


    @Test
    fun readAll_When_RelatedTermsFieldEmptyAndOtherFieldsFilled_Should_ReturnListWithOneKeytermWithAllThoseFields() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/RelatedTermsFieldEmptyAndOtherFieldsFilled.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("disciples"), listOf("student"), "Some notes.", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTerms_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommas() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTerms.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else", "completely different yet related thing")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithLeadingWhitespaceInTerm_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithLeadingWhitespaceInTerm.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithTrailingWhitespaceInTerm_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithTrailingWhitespaceInTerm.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithLeadingAndTrailingWhitespaceInTerm_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithLeadingAndTrailingWhitespaceInTerm.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithLeadingWhitespaceInField_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithLeadingWhitespaceInField.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithTrailingWhitespaceInField_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithTrailingWhitespaceInField.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithLeadingAndTrailingWhitespaceInField_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithLeadingAndTrailingWhitespaceInField.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithLeadingComma_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithEmptyRelatedTermsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithLeadingComma.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else", "completely different yet related thing")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithTrailingComma_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithEmptyRelatedTermsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithTrailingComma.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else", "completely different yet related thing")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithLeadingAndTrailingComma_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithEmptyRelatedTermsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithLeadingAndTrailingComma.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else", "completely different yet related thing")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithLeadingCommas_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithEmptyRelatedTermsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithLeadingCommas.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else", "completely different yet related thing")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithTrailingCommas_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithEmptyRelatedTermsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithTrailingCommas.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else", "completely different yet related thing")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleRelatedTermsWithLeadingAndTrailingCommas_Should_ReturnListWithOneKeytermWithListOfRelatedTermsSplitOnCommasWithEmptyRelatedTermsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleRelatedTermsWithLeadingAndTrailingCommas.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf(), listOf(), "", listOf("thing", "something else", "completely different yet related thing")))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherForms_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommas() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherForms.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else", "completely different yet related thing"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithLeadingWhitespaceInTerm_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithLeadingWhitespaceInTerm.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithTrailingWhitespaceInTerm_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithTrailingWhitespaceInTerm.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithLeadingAndTrailingWhitespaceInTerm_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithLeadingAndTrailingWhitespaceInTerm.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithLeadingWhitespaceInField_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithLeadingWhitespaceInField.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithTrailingWhitespaceInField_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithTrailingWhitespaceInField.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithLeadingAndTrailingWhitespaceInField_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithExtraWhitespaceRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithLeadingAndTrailingWhitespaceInField.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithLeadingComma_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithEmptyOtherFormsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithLeadingComma.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else", "completely different yet related thing"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithTrailingComma_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithEmptyOtherFormsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithTrailingComma.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else", "completely different yet related thing"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithLeadingAndTrailingComma_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithEmptyOtherFormsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithLeadingAndTrailingComma.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else", "completely different yet related thing"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithLeadingCommas_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithEmptyOtherFormsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithLeadingCommas.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else", "completely different yet related thing"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithTrailingCommas_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithEmptyOtherFormsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithTrailingCommas.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else", "completely different yet related thing"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    @Test
    fun readAll_When_MultipleOtherFormsWithLeadingAndTrailingCommas_Should_ReturnListWithOneKeytermWithListOfOtherFormsSplitOnCommasWithEmptyOtherFormsRemoved() {
        val keytermCsvReader = getKeytermCsvReaderFromResourcePath("KeytermCsvReader/MultipleOtherFormsWithLeadingAndTrailingCommas.csv")
        val expectedKeyterms: MutableList<Keyterm> = mutableListOf()
        expectedKeyterms.add(Keyterm("disciple", listOf("thing", "something else", "completely different yet related thing"), listOf(), "", listOf()))

        val actualKeyterms = keytermCsvReader.readAll()

        Assert.assertEquals(expectedKeyterms, actualKeyterms)
    }

    private fun getKeytermCsvReaderFromResourcePath(resource: String): KeytermCsvReader{
        val inputStream = this.javaClass.classLoader?.getResourceAsStream(resource)
        val streamReader = InputStreamReader(inputStream)
        return KeytermCsvReader(streamReader)
    }
}