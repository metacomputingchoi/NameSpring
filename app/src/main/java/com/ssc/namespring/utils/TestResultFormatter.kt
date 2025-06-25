// utils/TestResultFormatter.kt
package com.ssc.namespring.utils

import com.ssc.namespring.model.data.GeneratedName

class TestResultFormatter {

    companion object {
        private const val MAX_DISPLAY_RESULTS = 5
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
            println("${index + 1}. ${formatName(name)}")
            printNameDetails(name)
        }

        if (results.size > MAX_DISPLAY_RESULTS) {
            println("... 외 ${results.size - MAX_DISPLAY_RESULTS}개")
        }
    }

    fun printError(message: String) {
        System.err.println("[ERROR] $message")
    }

    private fun formatName(name: GeneratedName): String {
        return buildString {
            append("${name.surnameHangul}${name.combinedPronounciation}")
            append(" (${name.surnameHanja}${name.combinedHanja})")
        }
    }

    private fun printNameDetails(name: GeneratedName) {
        with(name) {
            println("   사격: 형(${fourTypes.hyung}), 원(${fourTypes.won}), 이(${fourTypes.i}), 정(${fourTypes.jung})")
            println("   획수: ${nameStrokes.joinToString(", ")}")
            println("   한자 상세:")
            hanjaDetails.forEachIndexed { idx, hanja ->
                println("     ${idx + 1}. ${hanja.hanja} - ${hanja.inmyongMeaning} (${hanja.inmyongSound})")
            }
        }
    }
}

// String 반복 확장 함수
private operator fun String.times(count: Int): String = repeat(count)