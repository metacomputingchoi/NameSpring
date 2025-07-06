// model/common/utils/MixedPatternUtils.kt
package com.ssc.namespring.model.common.utils

object MixedPatternUtils {

    // 혼합 패턴 여부 확인
    fun containsMixedPattern(text: String): Boolean {
        var hasChosung = false
        var hasHangul = false

        for (char in text) {
            when {
                char.toString().matches(Regex("[ㄱ-ㅎ]")) -> hasChosung = true
                char.toString().matches(Regex("[가-힣]")) -> hasHangul = true
            }

            if (hasChosung && hasHangul) return true
        }

        return hasChosung || hasHangul
    }

    // 혼합 패턴 매칭
    fun matchMixedPattern(text: String, pattern: String): Boolean {
        if (text.isEmpty() || pattern.isEmpty()) return false

        var textIdx = 0
        var patternIdx = 0

        while (textIdx < text.length && patternIdx < pattern.length) {
            val patternChar = pattern[patternIdx]
            val textChar = text[textIdx]

            when {
                // 패턴이 초성인 경우
                patternChar.toString().matches(Regex("[ㄱ-ㅎ]")) -> {
                    val textChosung = ChosungUtils.getChosung(textChar.toString())
                    if (textChosung == patternChar.toString()) {
                        textIdx++
                        patternIdx++
                    } else {
                        textIdx++
                        if (textIdx >= text.length) return false

                        patternIdx = 0
                        val remainingText = text.substring(textIdx)
                        return matchMixedPattern(remainingText, pattern)
                    }
                }
                // 패턴이 완성형 한글인 경우
                patternChar.toString().matches(Regex("[가-힣]")) -> {
                    if (textChar == patternChar) {
                        textIdx++
                        patternIdx++
                    } else {
                        textIdx++
                        if (textIdx >= text.length) return false

                        patternIdx = 0
                        val remainingText = text.substring(textIdx)
                        return matchMixedPattern(remainingText, pattern)
                    }
                }
                else -> {
                    patternIdx++
                }
            }
        }

        return patternIdx >= pattern.length
    }

    // 초성 패턴 매칭 (시작 또는 포함)
    fun matchChosungPattern(text: String, pattern: String): Boolean {
        val textChosung = text.map { ChosungUtils.getChosung(it.toString()) }.joinToString("")
        return textChosung.contains(pattern)
    }
}