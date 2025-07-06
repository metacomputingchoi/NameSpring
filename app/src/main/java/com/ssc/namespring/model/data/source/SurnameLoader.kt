// model/data/source/SurnameLoader.kt
package com.ssc.namespring.model.data.source

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.domain.entity.CharTripleInfoSurname
import java.io.BufferedReader
import java.io.InputStreamReader

class SurnameLoader(private val store: SurnameStore) {
    companion object {
        private const val TAG = "SurnameLoader"
    }

    fun loadData(context: Context) {
        val gson = Gson()

        try {
            loadSurnameHanjaMapping(context, gson)
            loadChosungMapping(context, gson)
            loadCharTripleDict(context, gson)

            // 초성 매핑 재구성
            rebuildChosungMapping()

            Log.d(TAG, "=== SurnameData 로드 완료 ===")
        } catch (e: Exception) {
            Log.e(TAG, "데이터 로드 실패", e)
            throw e
        }
    }

    // 초성 매핑을 charTripleDict에서 직접 생성
    private fun rebuildChosungMapping() {
        val newChosungMapping = mutableMapOf<String, MutableList<String>>()

        store.charTripleDict.forEach { (key, _) ->
            if (key.contains("/") && key.count { it == '/' } == 1) {
                val korean = key.split("/")[0]
                if (korean.length == 1) {
                    val chosung = getChosung(korean[0])
                    if (chosung.isNotEmpty()) {
                        newChosungMapping.getOrPut(chosung) { mutableListOf() }.add(korean)
                    }
                }
            }
        }

        // 기존 chosungMapping과 병합
        store.chosungMapping.forEach { (chosung, koreans) ->
            newChosungMapping.getOrPut(chosung) { mutableListOf() }.addAll(koreans)
        }

        // 중복 제거 및 Map으로 변환
        store.chosungMapping = newChosungMapping.mapValues { it.value.distinct() }
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

    // loadSurnameMapping 메서드 제거

    private fun loadSurnameHanjaMapping(context: Context, gson: Gson) {
        context.assets.open("surname/surname_hanja_pair_mapping_dict.json").use { stream ->
            BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                val type = object : TypeToken<Map<String, List<String>>>() {}.type
                store.surnameHanjaMapping = gson.fromJson(reader, type) ?: emptyMap()
                Log.d(TAG, "surnameHanjaMapping loaded: ${store.surnameHanjaMapping.size} entries")
            }
        }
    }

    private fun loadChosungMapping(context: Context, gson: Gson) {
        context.assets.open("surname/surname_chosung_to_korean_mapping.json").use { stream ->
            BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                val type = object : TypeToken<Map<String, List<String>>>() {}.type
                store.chosungMapping = gson.fromJson(reader, type) ?: emptyMap()
                Log.d(TAG, "chosungMapping loaded: ${store.chosungMapping.size} entries")
            }
        }
    }

    private fun loadCharTripleDict(context: Context, gson: Gson) {
        context.assets.open("surname/surname_char_triple_dict.json").use { stream ->
            BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                val type = object : TypeToken<Map<String, CharTripleInfoSurname>>() {}.type
                store.charTripleDict = gson.fromJson(reader, type) ?: emptyMap()
                Log.d(TAG, "charTripleDict loaded: ${store.charTripleDict.size} entries")
            }
        }
    }
}