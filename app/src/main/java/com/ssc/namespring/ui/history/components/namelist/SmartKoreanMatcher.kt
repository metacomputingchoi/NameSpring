// ui/history/components/namelist/SmartKoreanMatcher.kt
package com.ssc.namespring.ui.history.components.namelist

class SmartKoreanMatcher(
    private val enableFuzzySearch: Boolean = false
) {

    fun matchesDecomposed(name: String, query: String): Boolean {
        val parts = query.split(" ").filter { it.isNotEmpty() }
        if (parts.size < 2) return false

        var nameIdx = 0
        for (part in parts) {
            var found = false
            for (i in nameIdx until name.length) {
                if (matchesPart(name.substring(i), part)) {
                    nameIdx = i + part.length
                    found = true
                    break
                }
            }
            if (!found) return false
        }
        return true
    }

    private fun matchesPart(nameSubstring: String, part: String): Boolean {
        if (nameSubstring.startsWith(part)) return true

        // 초성 매칭
        if (part.all { KoreanJamoUtils.isChosung(it) }) {
            val nameChosung = KoreanJamoUtils.extractChosung(nameSubstring)
            return nameChosung.startsWith(part)
        }

        return false
    }

    fun matchesChosung(name: String, query: String): Boolean {
        // 순수 초성인지 체크
        if (!query.all { KoreanJamoUtils.isChosung(it) }) {
            // 초성이 아니면 이름의 초성 추출해서 비교
            val nameChosung = KoreanJamoUtils.extractChosung(name)
            return nameChosung.contains(query)
        }

        // 순수 초성 검색
        val nameChosung = KoreanJamoUtils.extractChosung(name)
        return nameChosung.contains(query)
    }

    fun matchesMixedPattern(name: String, query: String): Boolean {
        var nameIdx = 0

        for (qChar in query) {
            if (qChar == ' ') continue // 공백 무시

            var found = false
            for (i in nameIdx until name.length) {
                val matched = when {
                    KoreanJamoUtils.isChosung(qChar) -> KoreanJamoUtils.getChosungAt(name[i]) == qChar
                    KoreanJamoUtils.isJungsung(qChar) || KoreanJamoUtils.isJongsung(qChar) -> {
                        // 중성/종성은 해당 글자에 포함되어 있는지 체크
                        KoreanJamoUtils.containsJamo(name[i], qChar)
                    }
                    else -> name[i] == qChar
                }

                if (matched) {
                    nameIdx = i + 1
                    found = true
                    break
                }
            }

            if (!found) return false
        }

        return true
    }

    fun matchesJamoPattern(name: String, query: String): Boolean {
        // "ㅊㅗㅣ" → "최" 같은 패턴
        val assembled = KoreanJamoUtils.tryAssembleJamo(query)
        return assembled != query && name.contains(assembled)
    }

    fun isSimilarEnough(name: String, query: String): Boolean {
        if (query.length < 2) return false // 너무 짧은 쿼리는 유사도 검색 제외

        // 편집 거리가 1 이하인 경우만 허용
        for (i in name.indices) {
            if (i + query.length > name.length + 1) break

            val substring = name.substring(i, kotlin.math.min(i + query.length + 1, name.length))
            if (KoreanJamoUtils.editDistance(substring, query) <= 1) {
                return true
            }
        }

        return false
    }

    fun calculateRelevanceScore(name: String, query: String): Double {
        return when {
            name == query -> 100.0
            name.startsWith(query) -> 90.0
            name.contains(query) -> 80.0
            matchesChosung(name, query) -> 70.0
            matchesMixedPattern(name, query) -> 60.0
            enableFuzzySearch && isSimilarEnough(name, query) -> 50.0  // 오타 허용이 켜진 경우만
            else -> 0.0
        }
    }
}