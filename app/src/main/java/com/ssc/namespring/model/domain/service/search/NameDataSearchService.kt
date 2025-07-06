// model/domain/service/search/NameDataSearchService.kt
package com.ssc.namespring.model.domain.service.search

import android.util.Log
import com.ssc.namespring.model.domain.service.interfaces.SearchService
import com.ssc.namespring.model.domain.entity.HanjaSearchResult
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.HanjaInfo

class NameDataSearchService : SearchService<HanjaSearchResult> {
    companion object {
        private const val TAG = "NameDataSearchService"
    }

    private lateinit var optimizedMapping: OptimizedMapping

    fun initialize(mapping: OptimizedMapping) {
        this.optimizedMapping = mapping
    }

    fun getAllHanja(): List<HanjaSearchResult> {
        if (!::optimizedMapping.isInitialized) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        val allHanjaInfo = mutableListOf<HanjaInfo>()

        optimizedMapping.koreanToHanjaInfo.values.forEach { hanjaList ->
            allHanjaInfo.addAll(hanjaList)
        }

        return allHanjaInfo
            .distinctBy { it.tripleKey }
            .sortedBy { it.korean }
            .map { info ->
                HanjaSearchResult(
                    korean = info.korean,
                    hanja = info.hanja,
                    meaning = info.meaning,
                    ohaeng = info.ohaeng,
                    strokes = info.strokes,
                    soundCount = 1,
                    tripleKey = info.tripleKey
                )
            }
    }

    override fun search(query: String): List<HanjaSearchResult> {
        val normalizedQuery = query.trim()
        Log.d(TAG, "검색 쿼리: '$normalizedQuery'")

        // 빈 쿼리는 전체 목록 반환
        if (normalizedQuery.isEmpty()) {
            return getAllHanja()
        }

        val results = when {
            // 초성 검색 (ㄱ, ㄱㅅ 등)
            normalizedQuery.matches(Regex("^[ㄱ-ㅎ]+$")) -> searchByChosung(normalizedQuery)

            // 한글 검색 (단일 글자 또는 여러 글자)
            normalizedQuery.matches(Regex("^[가-힣]+$")) -> searchByKorean(normalizedQuery)

            // 한자 검색 (한자가 포함된 경우)
            containsHanja(normalizedQuery) -> searchByHanjaInternal(normalizedQuery)

            // 그 외는 모두 뜻 검색 (숫자, 영어, 한글 조합 등)
            else -> searchByMeaningInternal(normalizedQuery)
        }

        Log.d(TAG, "검색 결과: ${results.size}개")

        return results.map { info ->
            HanjaSearchResult(
                korean = info.korean,
                hanja = info.hanja,
                meaning = info.meaning,
                ohaeng = info.ohaeng,
                strokes = info.strokes,
                soundCount = 1,
                tripleKey = info.tripleKey
            )
        }
    }

    // 뜻으로만 검색하는 public 메서드
    fun searchByMeaning(query: String): List<HanjaSearchResult> {
        if (!::optimizedMapping.isInitialized) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        if (query.isEmpty()) {
            return emptyList()
        }

        val results = searchByMeaningInternal(query)

        return results.map { info ->
            HanjaSearchResult(
                korean = info.korean,
                hanja = info.hanja,
                meaning = info.meaning,
                ohaeng = info.ohaeng,
                strokes = info.strokes,
                soundCount = 1,
                tripleKey = info.tripleKey
            )
        }
    }

    // 한자로만 검색하는 public 메서드
    fun searchByHanja(query: String): List<HanjaSearchResult> {
        if (!::optimizedMapping.isInitialized) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        if (query.isEmpty()) {
            return emptyList()
        }

        val results = searchByHanjaInternal(query)

        return results.map { info ->
            HanjaSearchResult(
                korean = info.korean,
                hanja = info.hanja,
                meaning = info.meaning,
                ohaeng = info.ohaeng,
                strokes = info.strokes,
                soundCount = 1,
                tripleKey = info.tripleKey
            )
        }
    }

    // 한자 여부 판단 헬퍼 메서드
    private fun containsHanja(text: String): Boolean {
        return text.any { char ->
            val code = char.code
            // CJK Unified Ideographs 범위
            code in 0x4E00..0x9FFF ||
                    code in 0x3400..0x4DBF ||
                    code in 0x20000..0x2A6DF ||
                    code in 0x2A700..0x2B73F ||
                    code in 0x2B740..0x2B81F ||
                    code in 0x2B820..0x2CEAF ||
                    code in 0xF900..0xFAFF ||
                    code in 0x2F800..0x2FA1F
        }
    }

    private fun searchByChosung(query: String): List<HanjaInfo> {
        Log.d(TAG, "초성 검색 모드: $query")

        if (query.length == 1) {
            return optimizedMapping.chosungToHanjaInfo[query] ?: emptyList()
        }

        val results = mutableListOf<HanjaInfo>()
        optimizedMapping.koreanToHanjaInfo.forEach { (korean, hanjaList) ->
            if (matchesChosungPattern(korean, query)) {
                results.addAll(hanjaList)
            }
        }
        return results.distinctBy { it.tripleKey }
    }

    private fun matchesChosungPattern(text: String, pattern: String): Boolean {
        if (text.length < pattern.length) return false

        for (i in pattern.indices) {
            val textChar = text.getOrNull(i) ?: return false
            val textCharChosung = getChosung(textChar)
            if (textCharChosung != pattern[i].toString()) {
                return false
            }
        }
        return true
    }

    private fun getChosung(char: Char): String {
        val code = char.code - 0xAC00
        if (code < 0 || code > 11171) return ""

        val chosungList = arrayOf(
            "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ",
            "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
        )

        val index = code / 588
        return if (index in chosungList.indices) chosungList[index] else ""
    }

    private fun searchByKorean(query: String): List<HanjaInfo> {
        Log.d(TAG, "한글 검색 모드: $query")

        val exactMatch = optimizedMapping.koreanToHanjaInfo[query] ?: emptyList()

        // 부분 일치 검색
        if (query.length >= 2 && exactMatch.isEmpty()) {
            val partialMatches = mutableListOf<HanjaInfo>()
            optimizedMapping.koreanToHanjaInfo.forEach { (korean, hanjaList) ->
                if (korean.contains(query)) {
                    partialMatches.addAll(hanjaList)
                }
            }
            return partialMatches.distinctBy { it.tripleKey }
        }

        return exactMatch
    }

    private fun searchByHanjaInternal(query: String): List<HanjaInfo> {
        Log.d(TAG, "한자 검색 모드: $query")

        // 단일 한자 검색
        if (query.length == 1) {
            return optimizedMapping.hanjaToHanjaInfo[query] ?: emptyList()
        }

        // 한자가 포함된 복합 검색
        val results = mutableListOf<HanjaInfo>()
        optimizedMapping.hanjaToHanjaInfo.forEach { (hanja, infoList) ->
            if (query.contains(hanja)) {
                results.addAll(infoList)
            }
        }

        return results.distinctBy { it.tripleKey }
    }

    private fun searchByMeaningInternal(query: String): List<HanjaInfo> {
        Log.d(TAG, "뜻 검색 모드: $query")

        val results = mutableListOf<HanjaInfo>()

        // meaningSearchIndex에서 검색
        optimizedMapping.meaningSearchIndex.forEach { (word, hanjaList) ->
            if (word.contains(query, ignoreCase = true)) {
                results.addAll(hanjaList)
            }
        }

        // 직접 meaning 필드에서도 검색 (meaningSearchIndex에 없는 경우 대비)
        optimizedMapping.koreanToHanjaInfo.values.forEach { hanjaList ->
            hanjaList.forEach { info ->
                val meaning = info.meaning ?: ""
                if (meaning.contains(query, ignoreCase = true) &&
                    results.none { it.tripleKey == info.tripleKey }) {
                    results.add(info)
                }
            }
        }

        Log.d(TAG, "뜻 검색 결과: ${results.size}개")

        return results.distinctBy { it.tripleKey }
            .sortedBy { it.korean }
    }
}