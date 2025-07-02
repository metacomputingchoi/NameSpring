// com/ssc/namespring/model/NameData.kt
package com.ssc.namespring.model

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

object NameData {
    private const val TAG = "NameData"

    // 원본 트리플 데이터
    private var charTripleDict: Map<String, CharTripleInfo> = emptyMap()

    // 최적화된 검색 매핑
    private var optimizedMapping: OptimizedMapping? = null

    private var isInitialized = false

    data class OptimizedMapping(
        val version: String,
        val chosungToHanjaInfo: Map<String, List<HanjaInfo>>,
        val koreanToHanjaInfo: Map<String, List<HanjaInfo>>,
        val hanjaToHanjaInfo: Map<String, List<HanjaInfo>>,
        val meaningSearchIndex: Map<String, List<HanjaInfo>>,
        val stats: MappingStats
    )

    data class MappingStats(
        val totalTriples: Int,
        val totalProcessed: Int,
        val totalSkipped: Int,
        val totalChosung: Int,
        val totalKorean: Int,
        val totalHanja: Int,
        val totalMeaningWords: Int
    )

    data class HanjaInfo(
        val korean: String,
        val hanja: String,
        val meaning: String?,
        val ohaeng: String,
        val strokes: Int,
        val tripleKey: String,
        val nameMeaning: String?,
        val soundEumyang: Int,
        val strokeEumyang: Int,
        val soundOhaeng: String,
        val sourceOhaeng: String,
        val englishName: String,
        val cautionRed: String?,
        val cautionBlue: String?
    )

    data class CharTripleInfo(
        val koreanInfo: CharInfo,
        val hanjaInfo: CharInfo,
        val integratedInfo: IntegratedInfo
    )

    data class CharInfo(
        val character: String,
        val meaning: String?,
        val sound: String,
        val eumyang: Int,
        val ohaeng: String,
        val strokes: Int,
        val originalStrokes: Int
    )

    data class IntegratedInfo(
        val hanja: String,
        val nameMeaning: String?,
        val nameSound: String,
        val soundEumyang: Int,
        val strokeEumyang: Int,
        val soundOhaeng: String,
        val sourceOhaeng: String,
        val originalStrokes: Int,
        val dictionaryStrokes: Int,
        val englishName: String,
        val cautionRed: String?,
        val cautionBlue: String?
    )

    fun init(context: Context) {
        try {
            val gson = Gson()

            // 1. 최적화된 매핑 로드
            context.assets.open("name/name_optimized_search_mapping.json").use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    optimizedMapping = gson.fromJson(reader, OptimizedMapping::class.java)
                    Log.d(TAG, "최적화된 매핑 로드 완료")
                    Log.d(TAG, "통계: ${optimizedMapping?.stats}")
                }
            }

            // 2. 원본 트리플 데이터 로드 (상세 정보 조회용)
            context.assets.open("name/name_char_triple_dict_effective.json").use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    val type = object : TypeToken<Map<String, CharTripleInfo>>() {}.type
                    charTripleDict = gson.fromJson(reader, type)
                    Log.d(TAG, "트리플 딕셔너리 로드 완료: ${charTripleDict.size}개")
                }
            }

            isInitialized = true
            Log.d(TAG, "NameData 초기화 완료")

        } catch (e: Exception) {
            Log.e(TAG, "초기화 실패", e)
            isInitialized = false
            throw e
        }
    }

    fun searchHanja(query: String): List<HanjaSearchResult> {
        if (!isInitialized || optimizedMapping == null) {
            Log.e(TAG, "NameData가 초기화되지 않았습니다")
            return emptyList()
        }

        val normalizedQuery = query.trim()
        Log.d(TAG, "검색 쿼리: '$normalizedQuery'")

        val results = when {
            // 초성 검색
            normalizedQuery.matches(Regex("^[ㄱ-ㅎ]$")) -> {
                Log.d(TAG, "초성 검색 모드")
                val hanjaList = optimizedMapping?.chosungToHanjaInfo?.get(normalizedQuery) ?: emptyList()
                Log.d(TAG, "초성 '$normalizedQuery' 검색 결과: ${hanjaList.size}개")
                hanjaList
            }

            // 한글 검색
            normalizedQuery.matches(Regex("^[가-힣]$")) -> {
                Log.d(TAG, "한글 검색 모드")
                val hanjaList = optimizedMapping?.koreanToHanjaInfo?.get(normalizedQuery) ?: emptyList()
                Log.d(TAG, "한글 '$normalizedQuery' 검색 결과: ${hanjaList.size}개")
                hanjaList
            }

            // 한자 검색
            normalizedQuery.length == 1 -> {
                Log.d(TAG, "한자 검색 모드")
                val hanjaList = optimizedMapping?.hanjaToHanjaInfo?.get(normalizedQuery) ?: emptyList()
                Log.d(TAG, "한자 '$normalizedQuery' 검색 결과: ${hanjaList.size}개")
                hanjaList
            }

            // 뜻으로 검색 (2글자 이상)
            normalizedQuery.length >= 2 -> {
                Log.d(TAG, "뜻 검색 모드")
                val results = mutableListOf<HanjaInfo>()
                optimizedMapping?.meaningSearchIndex?.forEach { (word, hanjaList) ->
                    if (word.contains(normalizedQuery)) {
                        results.addAll(hanjaList)
                    }
                }
                Log.d(TAG, "뜻 '$normalizedQuery' 검색 결과: ${results.size}개")
                results.distinctBy { it.tripleKey }
            }

            else -> emptyList()
        }

        // HanjaInfo를 HanjaSearchResult로 변환
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

    fun getCharInfo(tripleKey: String): CharTripleInfo? {
        return charTripleDict[tripleKey]
    }

    fun getCharInfo(korean: String, hanja: String): CharTripleInfo? {
        val key = "$korean/$hanja"
        return charTripleDict[key]
    }

    fun validateData(): DataLoader.ValidationResult {
        val warnings = mutableListOf<String>()
        val criticalErrors = mutableListOf<String>()

        if (!isInitialized) {
            criticalErrors.add("NameData가 초기화되지 않음")
        }

        if (optimizedMapping == null) {
            criticalErrors.add("최적화된 매핑이 로드되지 않음")
        }

        return DataLoader.ValidationResult(
            isValid = criticalErrors.isEmpty(),
            warnings = warnings,
            criticalErrors = criticalErrors
        )
    }

    fun isReady(): Boolean = isInitialized

    fun getStats(): MappingStats? = optimizedMapping?.stats
}

data class HanjaSearchResult(
    val korean: String,
    val hanja: String,
    val meaning: String?,
    val ohaeng: String,
    val strokes: Int,
    val soundCount: Int,
    val tripleKey: String
)