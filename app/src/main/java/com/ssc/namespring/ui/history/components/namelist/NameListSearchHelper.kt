// ui/history/components/namelist/NameListSearchHelper.kt
package com.ssc.namespring.ui.history.components.namelist

import com.ssc.namingengine.data.GeneratedName

class NameListSearchHelper(
    private val enableFuzzySearch: Boolean = false // 기본적으로 오타 허용 비활성화
) {

    private val smartMatcher = SmartKoreanMatcher(enableFuzzySearch)

    fun filterNames(names: List<GeneratedName>, query: String): List<GeneratedName> {
        if (query.isBlank()) return names

        val trimmedQuery = query.trim()

        // 잘못된 한글 조합이면 빈 결과 반환
        if (isInvalidKoreanComposition(trimmedQuery)) {
            return emptyList()
        }

        // 스마트 검색 수행
        return names.filter { generatedName ->
            val fullName = "${generatedName.surnameHangul}${generatedName.combinedPronounciation}"
            smartMatch(fullName, trimmedQuery)
        }.sortedByDescending { generatedName ->
            // 검색 관련성 점수로 정렬
            val fullName = "${generatedName.surnameHangul}${generatedName.combinedPronounciation}"
            smartMatcher.calculateRelevanceScore(fullName, trimmedQuery)
        }
    }

    private fun smartMatch(name: String, query: String): Boolean {
        // 1. 정확한 매칭
        if (name.contains(query)) return true

        // 2. 불완전한 한글 처리 (예: "최ㅇ" → "최" + "ㅇ")
        val decomposedQuery = decomposeIncompleteKorean(query)
        if (decomposedQuery != query && smartMatcher.matchesDecomposed(name, decomposedQuery)) return true

        // 3. 공백 제거 매칭
        val queryNoSpace = query.replace(" ", "")
        if (queryNoSpace != query && name.contains(queryNoSpace)) return true

        // 4. 초성 검색
        if (smartMatcher.matchesChosung(name, query)) return true

        // 5. 혼합 패턴 검색
        if (smartMatcher.matchesMixedPattern(name, query)) return true

        // 6. 자모 분리 검색 (예: "ㅊㅗㅣ" → "최")
        if (smartMatcher.matchesJamoPattern(name, query)) return true

        // 7. 편집 거리 기반 유사도 검색 (오타 허용) - 옵션이 켜진 경우만
        if (enableFuzzySearch && smartMatcher.isSimilarEnough(name, query)) return true

        return false
    }

    private fun isInvalidKoreanComposition(text: String): Boolean {
        // 완성형 한글이 아닌 자모만 있는 경우 체크
        val hasOnlyJamo = text.all { it in 'ㄱ'..'ㅣ' }
        if (hasOnlyJamo && text.length > 3) return true

        // 불가능한 조합 체크 (예: "쵱", "됻" 등)
        return text.any { char ->
            if (char.code in 0xAC00..0xD7A3) {
                val code = char.code - 0xAC00
                val jong = code % 28
                val jung = (code / 28) % 21
                val cho = code / (21 * 28)

                // 불가능한 초성+중성 조합 체크
                isInvalidCombination(cho, jung, jong)
            } else false
        }
    }

    private fun isInvalidCombination(cho: Int, jung: Int, jong: Int): Boolean {
        // 실제 한글에서 매우 드물거나 사용하지 않는 조합들
        val invalidPatterns = setOf(
            Triple(18, 8, 0),  // ㅎ + ㅗ (회)는 가능하므로 제외
            Triple(0, 18, 27), // 극히 드문 조합들만 포함
            Triple(2, 19, 26),
            Triple(5, 20, 25)
        )

        // 쌍자음 + 특정 모음의 드문 조합
        if (cho in listOf(1, 4, 8, 10, 13)) { // ㄲ, ㄸ, ㅃ, ㅆ, ㅉ
            if (jung in listOf(1, 3, 5, 7, 11, 12, 14, 15, 16, 17)) {
                return true // 쌍자음과 잘 안쓰는 모음 조합
            }
        }

        return Triple(cho, jung, jong) in invalidPatterns
    }

    private fun decomposeIncompleteKorean(text: String): String {
        val result = StringBuilder()
        var i = 0

        while (i < text.length) {
            val char = text[i]

            if (char.code in 0xAC00..0xD7A3 && i + 1 < text.length) {
                val nextChar = text[i + 1]
                // 완성형 한글 다음에 자음/모음이 오는 경우
                if (nextChar in 'ㄱ'..'ㅣ') {
                    result.append(char)
                    result.append(' ') // 공백 삽입
                    result.append(nextChar)
                    i += 2
                    continue
                }
            }

            result.append(char)
            i++
        }

        return result.toString()
    }
}