// utils/JsonFileLoader.kt
package com.ssc.namespring.utils

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class JsonFileLoader(private val assetManager: AssetManager) {

    companion object {
        val JSON_FILES = mapOf(
            "ymd" to "ymd_data.json",
            "nameCharTriple" to "name_char_triple_dict_effective.json",
            "surnameCharTriple" to "surname_char_triple_dict.json",
            "nameKoreanToTriple" to "name_korean_to_triple_keys_mapping_effective.json",
            "nameHanjaToTriple" to "name_hanja_to_triple_keys_mapping_effective.json",
            "surnameKoreanToTriple" to "surname_korean_to_triple_keys_mapping.json",
            "surnameHanjaToTriple" to "surname_hanja_to_triple_keys_mapping.json",
            "surnameHanjaPair" to "surname_hanja_pair_mapping_dict.json",
            "dictHangulGivenNames" to "dict_hangul_given_names.json",
            "surnameChosungToKorean" to "surname_chosung_to_korean_mapping.json"
        )
    }

    suspend fun loadAllJsonFiles(): Map<String, String> = withContext(Dispatchers.IO) {
        JSON_FILES.mapValues { (_, fileName) ->
            loadJsonFile(fileName)
        }
    }

    private fun loadJsonFile(fileName: String): String {
        return try {
            assetManager.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IOException("$fileName 파일을 읽을 수 없습니다", e)
        }
    }
}