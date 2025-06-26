// utils/TestResultFormatter.kt
package com.ssc.namespring.utils

import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.FilteringStep

class TestResultFormatter {

    companion object {
        private const val MAX_DISPLAY_RESULTS = 5
        private const val MAX_EVALUATION_RESULTS = 10
    }

    fun printTestHeader(testInput: String) {
        println("=" * 80)
        println("테스트 입력: $testInput")
        println("=" * 80)
    }

    fun printTestResults(results: List<GeneratedName>, elapsedTime: Long) {
        println("\n=== 최종 결과: ${results.size}개 (소요시간: ${elapsedTime}ms) ===")

        if (results.isEmpty()) {
            println("생성된 이름이 없습니다.")
            return
        }

        results.take(MAX_DISPLAY_RESULTS).forEachIndexed { index, name ->
            println("\n[결과 ${index + 1}] ${formatName(name)}")
            printNameDetails(name)
            printAnalysisInfo(name, showFilteringDetails = false)
        }

        if (results.size > MAX_DISPLAY_RESULTS) {
            println("\n... 외 ${results.size - MAX_DISPLAY_RESULTS}개")
        }
    }

    fun printEvaluationResults(results: List<GeneratedName>, elapsedTime: Long) {
        println("\n=== 평가 결과: ${results.size}개 (소요시간: ${elapsedTime}ms) ===")

        if (results.isEmpty()) {
            println("평가된 이름이 없습니다.")
            return
        }

        val displayCount = minOf(results.size, MAX_EVALUATION_RESULTS)

        results.take(displayCount).forEachIndexed { index, name ->
            println("\n[평가 결과 ${index + 1}] ${formatName(name)}")
            printNameDetails(name)
            printAnalysisInfo(name, showFilteringDetails = true)
            printFilteringSteps(name)
        }

        if (results.size > displayCount) {
            println("\n... 외 ${results.size - displayCount}개")
        }
    }

    fun printError(message: String) {
        println("[X] [ERROR] $message")
    }

    private fun formatName(name: GeneratedName): String {
        return buildString {
            append("${name.surnameHangul}${name.combinedPronounciation}")
            append(" (${name.surnameHanja}${name.combinedHanja})")
        }
    }

    private fun printNameDetails(name: GeneratedName) {
        with(name) {
            println("   사격: 형(${sagyeok.hyeong}), 원(${sagyeok.won}), 이(${sagyeok.i}), 정(${sagyeok.jeong})")
            println("   획수: ${nameHanjaHoeksu.joinToString(", ")}")
            println("   한자 상세:")
            hanjaDetails.forEachIndexed { idx, hanja ->
                println("     ${idx + 1}. ${hanja.hanja} - ${hanja.inmyongMeaning} (${hanja.inmyongSound})")
                println("        발음음양: ${hanja.baleumEumyang}, 획수음양: ${hanja.hoeksuEumyang}")
                println("        발음오행: ${hanja.baleumOhaeng}, 자원오행: ${hanja.jawonOhaeng}")
                println("        원획수: ${hanja.wonHoeksu}, 옥편획수: ${hanja.okpyeonHoeksu}")
            }
        }
    }

    private fun printAnalysisInfo(name: GeneratedName, showFilteringDetails: Boolean) {
        name.analysisInfo?.let { analysisInfo ->
            println("\n   === 분석 정보 ===")

            // 사주 정보
            with(analysisInfo.sajuInfo) {
                println("   사주 오행: ${sajuOhaengCount}")
                if (missingElements.isNotEmpty()) {
                    println("   부족한 오행: ${missingElements.joinToString(", ")}")
                }
                if (dominantElements.isNotEmpty()) {
                    println("   과다한 오행: ${dominantElements.joinToString(", ")}")
                }
            }

            // 음양 정보
            with(analysisInfo.eumYangInfo) {
                println("   음양 분포: 음(${eumCount}개), 양(${yangCount}개) - ${balanceDescription}")
                println("   음양 패턴: $pattern")
                println("   균형 여부: ${if (isBalanced) "균형" else "불균형"}")
            }

            // 오행 정보
            with(analysisInfo.ohaengInfo) {
                println("   발음오행: $baleumOhaeng")
                println("   오행 조화도: $overallHarmony (점수: $harmonyScore)")
                if (generatingPairs.isNotEmpty()) {
                    println("   상생 관계: ${generatingPairs.joinToString(", ") { "${it.first}→${it.second}" }}")
                }
                if (conflictingPairs.isNotEmpty()) {
                    println("   상극 관계: ${conflictingPairs.joinToString(", ") { "${it.first}⇒${it.second}" }}")
                }
            }

            // 점수 정보
            println("   총점: ${analysisInfo.totalScore}점")
            println("   점수 구성: ${analysisInfo.scoreBreakdown}")

            // 추천사항
            if (analysisInfo.recommendations.isNotEmpty()) {
                println("   추천사항:")
                analysisInfo.recommendations.forEach { rec ->
                    println("     • $rec")
                }
            }
        } ?: println("   분석 정보 없음")
    }

    private fun printFilteringSteps(name: GeneratedName) {
        name.analysisInfo?.filteringSteps?.let { steps ->
            if (steps.isNotEmpty()) {
                println("\n   === 필터 평가 결과 ===")
                steps.forEach { step ->
                    printFilteringStep(step)
                }
            }
        }
    }

    private fun printFilteringStep(step: FilteringStep) {
        val status = if (step.passed) "✓ 통과" else "✗ 실패"
        println("   [$status] ${step.filterName}: ${step.reason}")

        if (step.details.isNotEmpty()) {
            step.details.forEach { (key, value) ->
                when (value) {
                    is Map<*, *> -> {
                        println("      $key:")
                        value.forEach { (k, v) ->
                            println("        - $k: $v")
                        }
                    }
                    is List<*> -> {
                        println("      $key: ${value.joinToString(", ")}")
                    }
                    else -> {
                        println("      $key: $value")
                    }
                }
            }
        }
    }
}

// String 반복 확장 함수
private operator fun String.times(count: Int): String = repeat(count)