// namegenerator/model/TestConfiguration.kt
package com.ssc.namespring.namegenerator.model

import java.time.LocalDateTime

data class TestConfiguration(
    val testCases: List<TestCase>,
    val defaultBirthDateTime: LocalDateTime,
    val testType: TestType
)

enum class TestType {
    GENERATION,     // 일반 생성 테스트
    EVALUATION      // 평가 테스트
}