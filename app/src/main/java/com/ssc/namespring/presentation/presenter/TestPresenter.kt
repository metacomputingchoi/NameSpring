// presentation/presenter/TestPresenter.kt
package com.ssc.namespring.presentation.presenter

import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.common.constants.Constants

class TestPresenter {

    fun presentTestCaseStart(testCase: TestCase) {
        println("\n${"=".repeat(Constants.SEPARATOR_LINE_LENGTH)}")
        println("테스트 케이스: ${testCase.name}")
        println("=".repeat(Constants.SEPARATOR_LINE_LENGTH))
    }

    fun presentTotalResults(count: Int) {
        println("총 결과 수: $count")
    }

    fun presentTargetFound(targetName: String) {
        println("\n✓ $targetName 발견!")
    }

    fun presentTargetNotFound(targetName: String) {
        println("\n✗ ${targetName}이 결과에 포함되지 않음!")
    }

    fun presentTargetScore(result: com.ssc.namespring.model.domain.name.entity.Name, score: Int) {
        println("  ${result.combinedHanja} ${result.combinedPronounciation} - 총점: ${score}점")
    }

    fun presentTopResults(results: List<NameSummary>) {
        if (results.isEmpty()) return

        println("\n결과 샘플 (상위 ${results.size}개):")
        results.forEachIndexed { i, summary ->
            println("${i + 1}. ${summary.hanja} ${summary.pronunciation} - 총점: ${summary.score}점")
        }
    }

    fun presentError(errorMessage: String?) {
        println("오류 발생: $errorMessage")
    }

    fun presentTestSummary(targetName: String) {
        println("\n${"=".repeat(Constants.SEPARATOR_LINE_LENGTH)}")
        println("테스트 결과 요약")
        println("=".repeat(Constants.SEPARATOR_LINE_LENGTH))
        println("모든 테스트 케이스에서 ${targetName}이 포함되는지 확인하십시오.")
        println("\n※ 성명학 점수는 참고용이며, 실제 이름 선택은 가족의 뜻과 개인의 선호를 종합적으로 고려하시기 바랍니다.")
    }
}