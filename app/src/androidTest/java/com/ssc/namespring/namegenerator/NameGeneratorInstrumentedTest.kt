// namegenerator/NameGeneratorInstrumentedTest.kt
package com.ssc.namespring.namegenerator

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.NameGeneratorModel
import com.ssc.namespring.namegenerator.controller.NameGeneratorTestController
import com.ssc.namespring.namegenerator.fixtures.TestFixtures
import com.ssc.namespring.namegenerator.view.TestResultView
import com.ssc.namespring.utils.logger.AndroidLogger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NameGeneratorInstrumentedTest {

    private lateinit var namingEngine: NamingEngine
    private lateinit var model: NameGeneratorModel
    private lateinit var view: TestResultView
    private lateinit var controller: NameGeneratorTestController

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        namingEngine = NamingEngine.create(
            logger = AndroidLogger("NamingEngineTest")
        )

        model = NameGeneratorModel(namingEngine)
        view = TestResultView()
        controller = NameGeneratorTestController(model, view)
    }

    @Test
    fun runAllDefaultTests() {
        val configurations = TestFixtures.getDefaultTestConfigurations()
        controller.runAllTests(configurations)
    }

    @Test
    fun runCustomTest() {
        val testCase = com.ssc.namespring.namegenerator.model.TestCase(
            input = "[이/李][_/_][_/_]",
            description = "이씨 두 글자 이름 테스트"
        )

        val result = controller.runSingleTest(
            testCase = testCase,
            defaultBirthDateTime = java.time.LocalDateTime.now()
        )

        assert(result.error == null)
        assert(result.results.isNotEmpty())
    }
}