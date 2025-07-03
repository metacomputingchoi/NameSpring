// model/repository/SurnameData.kt
package com.ssc.namespring.model.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.data.SurnameInfo
import java.io.BufferedReader
import java.io.InputStreamReader

object SurnameData {
    private const val TAG = "SurnameData"

    private var surnameMapping: Map<String, List<String>> = emptyMap()
    private var surnameHanjaMapping: Map<String, List<String>> = emptyMap()
    private var chosungMapping: Map<String, List<String>> = emptyMap()
    private var charTripleDict: Map<String, CharTripleInfo> = emptyMap()

    data class CharTripleInfo(
        @SerializedName("한글정보")
        val koreanInfo: CharInfo,
        @SerializedName("한자정보")
        val hanjaInfo: CharInfo,
        @SerializedName("통합정보")
        val integratedInfo: IntegratedInfo
    )

    data class CharInfo(
        @SerializedName("글자")
        val character: String,
        @SerializedName("뜻")
        val meaning: String?,
        @SerializedName("음")
        val sound: String,
        @SerializedName("음양")
        val eumyang: Int,
        @SerializedName("오행")
        val ohaeng: String,
        @SerializedName("획수")
        val strokes: Int,
        @SerializedName("원획수")
        val originalStrokes: Int
    )

    data class IntegratedInfo(
        @SerializedName("한자")
        val hanja: String,
        @SerializedName("인명용 뜻")
        val nameMeaning: String?,
        @SerializedName("인명용 음")
        val nameSound: String,
        @SerializedName("발음음양")
        val soundEumyang: Int,
        @SerializedName("획수음양")
        val strokeEumyang: Int,
        @SerializedName("발음오행")
        val soundOhaeng: String,
        @SerializedName("자원오행")
        val sourceOhaeng: String,
        @SerializedName("원획수")
        val originalStrokes: Int,
        @SerializedName("옥편획수")
        val dictionaryStrokes: Int,
        @SerializedName("E")
        val englishName: String,
        @SerializedName("CAUTION_RED")
        val cautionRed: String?,
        @SerializedName("CAUTION_BLUE")
        val cautionBlue: String?
    )

    fun init(context: Context) {
        val gson = Gson()

        try {
            context.assets.open("surname/surname_mapping.json").use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    val type = object : TypeToken<Map<String, List<String>>>() {}.type
                    surnameMapping = gson.fromJson(reader, type) ?: emptyMap()
                    Log.d(TAG, "surnameMapping loaded: ${surnameMapping.size} entries")
                }
            }

            context.assets.open("surname/surname_hanja_pair_mapping_dict.json").use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    val type = object : TypeToken<Map<String, List<String>>>() {}.type
                    surnameHanjaMapping = gson.fromJson(reader, type) ?: emptyMap()
                    Log.d(TAG, "surnameHanjaMapping loaded: ${surnameHanjaMapping.size} entries")
                }
            }

            context.assets.open("surname/surname_chosung_to_korean_mapping.json").use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    val type = object : TypeToken<Map<String, List<String>>>() {}.type
                    chosungMapping = gson.fromJson(reader, type) ?: emptyMap()
                    Log.d(TAG, "chosungMapping loaded: ${chosungMapping.size} entries")
                }
            }

            context.assets.open("surname/surname_char_triple_dict.json").use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    val type = object : TypeToken<Map<String, CharTripleInfo>>() {}.type
                    charTripleDict = gson.fromJson(reader, type) ?: emptyMap()
                    Log.d(TAG, "charTripleDict loaded: ${charTripleDict.size} entries")
                }
            }

            Log.d(TAG, "=== SurnameData 로드 완료 ===")

        } catch (e: Exception) {
            Log.e(TAG, "데이터 로드 실패", e)
            throw e
        }
    }

    fun validateData(): DataLoader.ValidationResult {
        val warnings = mutableListOf<String>()
        val criticalErrors = mutableListOf<String>()

        Log.d(TAG, "=== 성씨 데이터 검증 시작 ===")

        if (charTripleDict.isEmpty()) {
            criticalErrors.add("성씨 charTripleDict가 비어있음")
        }
        if (surnameMapping.isEmpty()) {
            criticalErrors.add("surnameMapping이 비어있음")
        }

        var missingCount = 0
        surnameMapping.forEach { (korean, hanjaList) ->
            hanjaList.forEach { hanja ->
                val key = "$korean/$hanja"
                if (!charTripleDict.containsKey(key)) {
                    missingCount++
                    Log.v(TAG, "Missing in charTripleDict: $key")
                }
            }
        }

        if (missingCount > 0) {
            warnings.add("성씨 charTripleDict에서 $missingCount 개의 키 누락")
        }

        var invalidCompoundCount = 0
        surnameHanjaMapping.forEach { (key, parts) ->
            if (key.contains("/") && key.count { it == '/' } == 1) {
                parts.forEach { partKey ->
                    if (!charTripleDict.containsKey(partKey)) {
                        invalidCompoundCount++
                        Log.v(TAG, "Invalid compound surname part: $partKey in $key")
                    }
                }
            }
        }

        if (invalidCompoundCount > 0) {
            warnings.add("복성 매핑 경고: $invalidCompoundCount 개")
        }

        Log.d(TAG, "=== 성씨 데이터 검증 완료 ===")
        Log.d(TAG, "경고: ${warnings.size}개, 치명적 오류: ${criticalErrors.size}개")

        return DataLoader.ValidationResult(
            isValid = warnings.isEmpty() && criticalErrors.isEmpty(),
            warnings = warnings,
            criticalErrors = criticalErrors
        )
    }

    fun searchSurnames(query: String): List<SurnameSearchResult> {
        if (!DataLoader.isReady()) {
            Log.e(TAG, "데이터가 아직 로드되지 않았습니다")
            return emptyList()
        }

        val results = mutableListOf<SurnameSearchResult>()

        when {
            query.matches(Regex("[ㄱ-ㅎ]+")) -> {
                if (query.length == 1) {
                    chosungMapping[query]?.forEach { korean ->
                        addSurnameResults(korean, results)

                        surnameHanjaMapping.keys
                            .filter { it.contains("/") && it.startsWith(korean) && it.count { c -> c == '/' } == 1 }
                            .forEach { compoundKey ->
                                val parts = compoundKey.split("/")
                                if (parts[0].length > 1) {
                                    addCompoundSurnameResult(parts[0], parts[1], results)
                                }
                            }
                    }
                } else if (query.length == 2) {
                    val firstChosung = query[0].toString()
                    val secondChosung = query[1].toString()

                    surnameHanjaMapping.keys
                        .filter { it.contains("/") && it.count { c -> c == '/' } == 1 }
                        .forEach { compoundKey ->
                            val parts = compoundKey.split("/")
                            val korean = parts[0]
                            if (korean.length == 2) {
                                if (getChosung(korean[0].toString()) == firstChosung &&
                                    getChosung(korean[1].toString()) == secondChosung) {
                                    addCompoundSurnameResult(korean, parts[1], results)
                                }
                            }
                        }
                }
            }
            query.matches(Regex("[가-힣]+")) -> {
                surnameHanjaMapping.keys
                    .filter { it.startsWith("$query/") }
                    .forEach { key ->
                        val parts = key.split("/")
                        if (parts[0] == query && query.length > 1) {
                            addCompoundSurnameResult(query, parts[1], results)
                        }
                    }

                if (query.length == 1) {
                    surnameHanjaMapping.keys
                        .filter { it.contains("/") && it.startsWith(query) && it.count { c -> c == '/' } == 1 }
                        .forEach { compoundKey ->
                            val parts = compoundKey.split("/")
                            if (parts[0].length > 1) {
                                addCompoundSurnameResult(parts[0], parts[1], results)
                            }
                        }
                }

                addSurnameResults(query, results)
            }
            else -> {
                charTripleDict.entries.forEach { (key, value) ->
                    if (value.hanjaInfo.character.contains(query)) {
                        results.add(SurnameSearchResult(
                            korean = value.koreanInfo.character,
                            hanja = value.hanjaInfo.character,
                            meaning = value.integratedInfo.nameMeaning,
                            isCompound = false
                        ))
                    }
                }

                surnameHanjaMapping.keys
                    .filter { it.contains("/") && it.split("/")[1].contains(query) }
                    .forEach { key ->
                        val parts = key.split("/")
                        if (parts[0].length > 1) {
                            addCompoundSurnameResult(parts[0], parts[1], results)
                        }
                    }
            }
        }

        return results.distinctBy { "${it.korean}/${it.hanja}" }
            .sortedWith(compareBy(
                { !it.isCompound },
                { it.korean }
            ))
    }

    private fun getChosung(text: String): String {
        if (text.isEmpty()) return ""
        val char = text[0]
        val code = char.code - 0xAC00
        if (code < 0 || code > 11171) return ""
        return CHOSUNG_LIST[code / 588]
    }

    private val CHOSUNG_LIST = arrayOf(
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ",
        "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    )

    private fun addCompoundSurnameResult(korean: String, hanja: String, results: MutableList<SurnameSearchResult>) {
        val compoundKey = "$korean/$hanja"

        surnameHanjaMapping[compoundKey]?.let { parts ->
            if (parts.size >= 2) {
                val meanings = mutableListOf<String>()
                parts.forEach { partKey ->
                    charTripleDict[partKey]?.integratedInfo?.nameMeaning?.let {
                        meanings.add(it)
                    }
                }

                results.add(SurnameSearchResult(
                    korean = korean,
                    hanja = hanja,
                    meaning = meanings.joinToString(" ").ifEmpty { null },
                    isCompound = true
                ))
            }
        }
    }

    private fun addSurnameResults(korean: String, results: MutableList<SurnameSearchResult>) {
        surnameMapping[korean]?.forEach { hanja ->
            val key = "$korean/$hanja"
            charTripleDict[key]?.let { info ->
                results.add(SurnameSearchResult(
                    korean = korean,
                    hanja = hanja,
                    meaning = info.integratedInfo.nameMeaning,
                    isCompound = false
                ))
            }
        }
    }

    fun getSurnameInfo(korean: String, hanja: String): SurnameInfo? {
        val key = "$korean/$hanja"

        if (korean.length > 1) {
            surnameHanjaMapping[key]?.let { parts ->
                val meanings = mutableListOf<String>()
                var totalStrokes = 0
                var firstOhaeng: String? = null
                var firstEumyang = 0

                parts.forEach { partKey ->
                    charTripleDict[partKey]?.let { info ->
                        info.integratedInfo.nameMeaning?.let { meanings.add(it) }
                        totalStrokes += info.hanjaInfo.strokes
                        if (firstOhaeng == null) {
                            firstOhaeng = info.hanjaInfo.ohaeng
                            firstEumyang = info.hanjaInfo.eumyang
                        }
                    }
                }

                return SurnameInfo(
                    korean = korean,
                    hanja = hanja,
                    meaning = meanings.joinToString(" ").ifEmpty { null },
                    strokes = totalStrokes,
                    ohaeng = firstOhaeng,
                    eumyang = firstEumyang
                )
            }
        }

        return charTripleDict[key]?.let { info ->
            SurnameInfo(
                korean = korean,
                hanja = hanja,
                meaning = info.integratedInfo.nameMeaning,
                strokes = info.hanjaInfo.strokes,
                ohaeng = info.hanjaInfo.ohaeng,
                eumyang = info.hanjaInfo.eumyang
            )
        }
    }
}

data class SurnameSearchResult(
    val korean: String,
    val hanja: String,
    val meaning: String?,
    val isCompound: Boolean
)
