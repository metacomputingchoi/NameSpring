// model/common/utils/EnhancedSearchHelper.kt
package com.ssc.namespring.model.common.utils

object EnhancedSearchHelper {

    /**
     * 두 문자열 간의 편집 거리(Levenshtein Distance) 계산
     */
    fun calculateEditDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // 삭제
                    dp[i][j - 1] + 1,      // 삽입
                    dp[i - 1][j - 1] + cost // 치환
                )
            }
        }

        return dp[len1][len2]
    }

    /**
     * 유사도 점수 계산 (0.0 ~ 1.0)
     */
    fun calculateSimilarity(s1: String, s2: String): Float {
        if (s1.isEmpty() || s2.isEmpty()) return 0f

        val maxLen = maxOf(s1.length, s2.length)
        val editDistance = calculateEditDistance(s1, s2)

        return 1f - (editDistance.toFloat() / maxLen)
    }

    /**
     * 자모 분리를 통한 한글 유사도 계산
     */
    fun calculateKoreanSimilarity(s1: String, s2: String): Float {
        val jamo1 = s1.flatMap { decomposeHangul(it) }
        val jamo2 = s2.flatMap { decomposeHangul(it) }

        if (jamo1.isEmpty() || jamo2.isEmpty()) return 0f

        val maxLen = maxOf(jamo1.size, jamo2.size)
        var matchCount = 0

        val minLen = minOf(jamo1.size, jamo2.size)
        for (i in 0 until minLen) {
            if (jamo1[i] == jamo2[i]) matchCount++
        }

        return matchCount.toFloat() / maxLen
    }

    /**
     * 한글 자모 분리
     */
    private fun decomposeHangul(char: Char): List<Char> {
        if (char !in '가'..'힣') return listOf(char)

        val code = char.code - 0xAC00
        val chosung = code / 588
        val jungsung = (code % 588) / 28
        val jongsung = code % 28

        val chosungList = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
        val jungsungList = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
        val jongsungList = " ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ"

        val result = mutableListOf<Char>()
        result.add(chosungList[chosung])
        result.add(jungsungList[jungsung])
        if (jongsung > 0) {
            result.add(jongsungList[jongsung])
        }

        return result
    }
}