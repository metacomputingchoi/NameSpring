// model/TestCase.kt
package com.ssc.namespring.model

import java.time.LocalDateTime
import com.ssc.namingengine.data.GeneratedName

data class TestCase(
    val input: String,
    val description: String? = null,
    val birthDateTime: LocalDateTime? = null,
    val withoutFilter: Boolean = false
)

data class TestConfiguration(
    val testCases: List<TestCase>,
    val defaultBirthDateTime: LocalDateTime,
    val testType: TestType
)

enum class TestType {
    GENERATION,     // 일반 생성 테스트
    EVALUATION      // 평가 테스트
}

data class TestResult(
    val testCase: TestCase,
    val results: List<GeneratedName>,
    val elapsedTime: Long,
    val error: String? = null
)