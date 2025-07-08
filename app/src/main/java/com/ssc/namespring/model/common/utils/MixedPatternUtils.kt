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

    // 혼합 패턴 매칭 (개선된 버전)
    fun matchMixedPattern(text: String, pattern: String): Boolean {
        if (text.isEmpty() || pattern.isEmpty()) return false

        // 띄어쓰기를 무시한 버전도 시도
        val textNoSpace = text.replace(" ", "")
        val patternNoSpace = pattern.replace(" ", "")

        return matchMixedPatternInternal(text, pattern) ||
                matchMixedPatternInternal(textNoSpace, patternNoSpace)
    }

    private fun matchMixedPatternInternal(text: String, pattern: String): Boolean {
        var textIdx = 0
        var patternIdx = 0

        while (textIdx < text.length && patternIdx < pattern.length) {
            val patternChar = pattern[patternIdx]
            val textChar = text[textIdx]

            when {
                // 띄어쓰기는 스킵
                patternChar == ' ' -> {
                    patternIdx++
                    continue
                }
                textChar == ' ' -> {
                    textIdx++
                    continue
                }
                // 패턴이 초성인 경우
                patternChar.toString().matches(Regex("[ㄱ-ㅎ]")) -> {
                    val textChosung = ChosungUtils.getChosung(textChar.toString())
                    if (textChosung == patternChar.toString()) {
                        textIdx++
                        patternIdx++
                    } else {
                        // 매치 실패 시 다음 위치에서 재시도
                        textIdx++
                        if (textIdx >= text.length) return false
                        patternIdx = 0
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
                    }
                }
                else -> {
                    patternIdx++
                }
            }
        }

        return patternIdx >= pattern.length
    }

    // 초성 패턴 매칭 (개선된 버전)
    fun matchChosungPattern(text: String, pattern: String): Boolean {
        // 띄어쓰기를 포함한 초성 변환
        val textChosung = text.map {
            when {
                it == ' ' -> ' '
                it.toString().matches(Regex("[가-힣]")) -> ChosungUtils.getChosung(it.toString())
                else -> ""
            }
        }.joinToString("")

        // 띄어쓰기가 있는 패턴과 없는 패턴 모두 확인
        val textChosungNoSpace = textChosung.replace(" ", "")
        val patternNoSpace = pattern.replace(" ", "")

        return textChosung.contains(pattern) || textChosungNoSpace.contains(patternNoSpace)
    }
}