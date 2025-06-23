// model/NameAnalyzer.kt
package com.ssc.namespring.model

import android.content.Context
import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.loader.DataLoader
import com.ssc.namespring.model.calculator.SajuCalculator
import com.ssc.namespring.model.analyzer.NameCombinationAnalyzer
import com.ssc.namespring.model.filter.NameFilter
import com.ssc.namespring.model.calculator.ScoreCalculator
import com.ssc.namespring.model.report.ReportGenerator
import com.ssc.namespring.model.constants.Constants

class NameAnalyzer(private val context: Context) {

    private val dataLoader = DataLoader(context)
    private val sajuCalculator = SajuCalculator()
    private val nameAnalyzer = NameCombinationAnalyzer(dataLoader.hanja2Stroke)
    private val nameFilter = NameFilter(dataLoader)
    private val scoreCalculator = ScoreCalculator()
    private val reportGenerator = ReportGenerator()

    fun get4Ju(year: Int, month: Int, day: Int, hour: Int, minute: Int): FourJu {
        return sajuCalculator.get4Ju(year, month, day, hour, minute, dataLoader.ymdData)
    }

    fun getDictElementsCount(fourJu: FourJu): ElementCount {
        return sajuCalculator.getDictElementsCount(fourJu)
    }

    fun findNamesGeneralized(
        surHangul: String?,
        surHanja: String,
        name1Hangul: String? = null,
        name1Hanja: String? = null,
        name2Hangul: String? = null,
        name2Hanja: String? = null,
        dictElementsCount: ElementCount? = null,
        birthYear: Int = Constants.DEFAULT_BIRTH_YEAR,
        birthMonth: Int = Constants.DEFAULT_BIRTH_MONTH,
        birthDay: Int = Constants.DEFAULT_BIRTH_DAY,
        birthHour: Int = Constants.DEFAULT_BIRTH_HOUR,
        birthMinute: Int = Constants.DEFAULT_BIRTH_MINUTE
    ): List<NameResult> {
        // 한글 성씨가 없으면 한자 성씨에서 찾기
        val finalSurHangul = surHangul ?: dataLoader.getHangulSurnameFromHanja(surHanja)

        // 사주 계산
        val fourJu = get4Ju(birthYear, birthMonth, birthDay, birthHour, birthMinute)
        val finalDictElementsCount = dictElementsCount ?: getDictElementsCount(fourJu)

        // 좋은 조합 찾기
        val goodCombinations = nameAnalyzer.analyzeNameCombinations(finalSurHangul, surHanja)

        // 0개와 1개 오행 추출
        val zeroElements = finalDictElementsCount.toMap()
            .filter { it.value == 0 }
            .map { it.key }

        val oneElements = finalDictElementsCount.toMap()
            .filter { it.value == 1 }
            .map { it.key }

        // 결과 생성
        return nameFilter.filterNames(
            goodCombinations,
            finalSurHangul,
            surHanja,
            name1Hangul,
            name1Hanja,
            name2Hangul,
            name2Hanja,
            fourJu,
            finalDictElementsCount,
            zeroElements,
            oneElements,
            BirthInfo(birthYear, birthMonth, birthDay, birthHour, birthMinute)
        )
    }

    fun calculateDetailedScore(result: NameResult): NameScore {
        return scoreCalculator.calculateDetailedScore(result)
    }

    fun generateNameExplanation(result: NameResult): Pair<NameExplanation, NameScore> {
        return reportGenerator.generateNameExplanation(result, scoreCalculator)
    }

    fun printDetailedNameReport(result: NameResult) {
        reportGenerator.printDetailedNameReport(result, scoreCalculator)
    }

    fun runTestCases() {
        val testCases = listOf(
            TestCase("1. 성/한자, ㅁ/ㅁ, ㅁ/ㅁ", "김", "金", null, null, null, null),
            TestCase("2. 성/한자, 극/ㅁ, ㅁ/ㅁ", "김", "金", "극", null, null, null),
            TestCase("3. 성/한자, ㅁ/ㅁ, 범/ㅁ", "김", "金", null, null, "범", null),
            TestCase("4. 성/한자, 극/ㅁ, 범/ㅁ", "김", "金", "극", null, "범", null),
            TestCase("5. 성/한자, 극/克, ㅁ/ㅁ", "김", "金", null, "克", null, null),
            TestCase("6. 성/한자, ㅁ/ㅁ, 범/訉", "김", "金", null, null, null, "訉"),
            TestCase("7. 성/한자, 극/克, 범/ㅁ", "김", "金", null, "克", "범", null),
            TestCase("8. 성/한자, 극/ㅁ, 범/訉", "김", "金", "극", null, null, "訉"),
            TestCase("9. 성/한자, 극/克, 범/訉", "김", "金", null, "克", null, "訉"),
            TestCase("10. ㅁ/한자(金), ㅁ/ㅁ, ㅁ/ㅁ (한자성만)", null, "金", null, null, null, null),
            TestCase("11. ㅁ/한자(姜), ㅁ/ㅁ, ㅁ/ㅁ (다중 한글 매핑)", null, "姜", null, null, null, null)
        )

        val fourJu = get4Ju(Constants.DEFAULT_BIRTH_YEAR, Constants.DEFAULT_BIRTH_MONTH,
            Constants.DEFAULT_BIRTH_DAY, Constants.DEFAULT_BIRTH_HOUR,
            Constants.DEFAULT_BIRTH_MINUTE)
        val dictElementsCount = getDictElementsCount(fourJu)

        val targetName = "克訉"
        val targetCaseIndex = Constants.TARGET_CASE_INDEX

        testCases.forEachIndexed { idx, testCase ->
            println("\n${"=".repeat(Constants.SEPARATOR_LINE_LENGTH)}")
            println("테스트 케이스: ${testCase.name}")
            println("=".repeat(Constants.SEPARATOR_LINE_LENGTH))

            try {
                val results = findNamesGeneralized(
                    testCase.surHangul, testCase.surHanja,
                    testCase.name1Hangul, testCase.name1Hanja,
                    testCase.name2Hangul, testCase.name2Hanja,
                    dictElementsCount
                )

                println("총 결과 수: ${results.size}")

                // 김극범(克訉)이 포함되는지 확인
                var found = false
                for (result in results) {
                    if (result.combinedHanja == targetName) {
                        found = true
                        println("\n✓ 김극범(克訉) 발견!")

                        // 특정 케이스에 대해서만 상세 보고서 출력
                        if (idx == targetCaseIndex) {
                            printDetailedNameReport(result)
                        } else {
                            val score = calculateDetailedScore(result).total
                            println("  ${result.combinedHanja} ${result.combinedPronounciation} - 총점: ${score}점")
                        }
                        break
                    }
                }

                if (!found) {
                    println("\n✗ 김극범(克訉)이 결과에 포함되지 않음!")
                }

                if (results.isNotEmpty() && idx != targetCaseIndex) {
                    println("\n결과 샘플 (상위 ${Constants.TOP_RESULTS_COUNT}개):")
                    results.take(Constants.TOP_RESULTS_COUNT).forEachIndexed { i, result ->
                        val score = calculateDetailedScore(result).total
                        println("${i + 1}. ${result.combinedHanja} ${result.combinedPronounciation} - 총점: ${score}점")
                    }
                }

            } catch (e: Exception) {
                println("오류 발생: ${e.message}")
                e.printStackTrace()
            }
        }

        println("\n${"=".repeat(Constants.SEPARATOR_LINE_LENGTH)}")
        println("테스트 결과 요약")
        println("=".repeat(Constants.SEPARATOR_LINE_LENGTH))
        println("모든 테스트 케이스에서 김극범(克訉)이 포함되는지 확인하십시오.")
        println("\n※ 성명학 점수는 참고용이며, 실제 이름 선택은 가족의 뜻과 개인의 선호를 종합적으로 고려하시기 바랍니다.")
    }

    data class TestCase(
        val name: String,
        val surHangul: String?,
        val surHanja: String,
        val name1Hangul: String?,
        val name1Hanja: String?,
        val name2Hangul: String?,
        val name2Hanja: String?
    )
}