// namegenerator/fixtures/TestFixtures.kt
package com.ssc.namespring.namegenerator.fixtures

import com.ssc.namespring.namegenerator.model.TestCase
import com.ssc.namespring.namegenerator.model.TestConfiguration
import com.ssc.namespring.namegenerator.model.TestType
import java.time.LocalDateTime

object TestFixtures {

    fun getDefaultTestConfigurations(): List<TestConfiguration> {
        return listOf(
            TestConfiguration(
                testCases = listOf(
                    TestCase("[김/金][_/_]"),
                    TestCase("[김/金][_/_][_/_]"),
                    TestCase("[김/金][ㅁ/_][_/_]"),
                    TestCase("[김/金][_/_][ㄱ/_]"),
                    TestCase("[김/金][민/_][_/_]"),
                    TestCase("[김/金][_/岷][_/_]"),
                    TestCase("[김/金][민/岷][_/_]"),
                    TestCase("[남궁/南宮][_/_][_/_]"),
                    TestCase("[남궁/南宮][_/_]"),
                    TestCase("[김/金][민/岷][구/枸]")
                ),
                defaultBirthDateTime = LocalDateTime.of(2025, 6, 11, 14, 30, 0),
                testType = TestType.GENERATION
            ),
            TestConfiguration(
                testCases = listOf(
                    TestCase(
                        input = "[최/崔][성/成][수/_]",
                        description = "최성수 - 높을 최(崔), 이룰 성(成), 수(한자 비움)",
                        withoutFilter = true
                    ),
                    TestCase(
                        input = "[최/崔][성/成][수/秀]",
                        description = "최성수 - 높을 최(崔), 이룰 성(成), 빼어날 수(秀)",
                        withoutFilter = true
                    )
                ),
                defaultBirthDateTime = LocalDateTime.of(1986, 4, 19, 5, 45, 0),
                testType = TestType.EVALUATION
            ),
            TestConfiguration(
                testCases = listOf(
                    TestCase(
                        input = "[김/金][우/禹][현/鉉]",
                        description = "김우현 - 쇠 김(金), 하우씨 우(禹), 솥귀 현(鉉)",
                        withoutFilter = true
                    )
                ),
                defaultBirthDateTime = LocalDateTime.of(1989, 1, 10, 1, 15, 0),
                testType = TestType.EVALUATION
            )
        )
    }
}