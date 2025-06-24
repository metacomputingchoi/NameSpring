// model/data/NameAnalyzerData.kt
package com.ssc.namespring.model.data

import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore

data class TestCase(
    val name: String,
    val surHangul: String?,
    val surHanja: String,
    val name1Hangul: String?,
    val name1Hanja: String?,
    val name2Hangul: String?,
    val name2Hanja: String?
)

data class TestCaseResult(
    val testCase: TestCase,
    val index: Int,
    val totalResults: Int,
    val targetFound: Boolean,
    val targetNameInfo: Name? = null,
    val targetScore: Int? = null,
    val topResults: List<NameSummary> = emptyList(),
    val error: String? = null,
    val executionTimeMs: Long = 0
)

data class NameSummary(
    val hanja: String?,
    val pronunciation: String?,
    val score: Int
)

data class TestSuiteResult(
    val testResults: List<TestCaseResult>,
    val summary: TestSummary,
    val executionTimeMs: Long,
    val fourJu: Saju,
    val dictElementsCount: ElementBalance,
    val targetName: String,
    val targetCaseIndex: Int
)

data class TestSummary(
    val totalTests: Int,
    val successCount: Int,
    val errorCount: Int,
    val targetFoundCount: Int
)

data class AnalysisResult(
    val names: List<Name>,
    val totalNames: Int,
    val targetName: String?,
    val targetFound: Boolean,
    val targetNameInfo: Name? = null,
    val targetScore: NameScore?,
    val fourJu: Saju,
    val elementBalance: ElementBalance
)

data class TestConfig(
    val targetName: String,
    val targetCaseIndex: Int,
    val birthInfo: BirthInfo,
    val testCases: List<TestCase>
)

data class BirthInfo(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
)