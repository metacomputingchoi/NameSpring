// namegenerator/NameGeneratorUnitTest.kt
package com.ssc.namespring.namegenerator

import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.NameGeneratorModel
import com.ssc.namespring.namegenerator.fixtures.TestFixtures
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class NameGeneratorUnitTest {

    private lateinit var namingEngine: NamingEngine
    private lateinit var model: NameGeneratorModel

    @Before
    fun setup() {
        namingEngine = NamingEngine.create()
        model = NameGeneratorModel(namingEngine)
    }

    @Test
    fun testGenerationWithSingleCharacter() {
        val result = model.generateNames(
            userInput = "[김/金][_/_]",
            birthDateTime = LocalDateTime.of(2025, 6, 11, 14, 30, 0),
            useYajasi = true,
            verbose = false
        )

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testEvaluationWithCompleteNames() {
        val result = model.generateNames(
            userInput = "[최/崔][성/成][수/秀]",
            birthDateTime = LocalDateTime.of(1986, 4, 19, 5, 45, 0),
            withoutFilter = true
        )

        assertNotNull(result)
        assertEquals(1, result.size) // 평가는 입력된 이름만 반환
    }

    @Test
    fun testAllDefaultConfigurations() {
        val configurations = TestFixtures.getDefaultTestConfigurations()

        configurations.forEach { config ->
            config.testCases.forEach { testCase ->
                val birthDateTime = testCase.birthDateTime ?: config.defaultBirthDateTime

                val result = model.generateNames(
                    userInput = testCase.input,
                    birthDateTime = birthDateTime,
                    withoutFilter = testCase.withoutFilter
                )

                assertNotNull(result)
            }
        }
    }

    @Test
    fun testValidation() {
        val validInput = "[김/金][민/岷][수/秀]"
        val invalidInput = "invalid input"

        val validResult = model.validateInput(validInput)
        assertTrue(validResult.isValid)

        val invalidResult = model.validateInput(invalidInput)
        assertFalse(invalidResult.isValid)
    }
}