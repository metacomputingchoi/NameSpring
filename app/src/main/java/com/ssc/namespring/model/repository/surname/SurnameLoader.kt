// model/repository/surname/SurnameLoader.kt
package com.ssc.namespring.model.repository.surname

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class SurnameLoader(private val store: SurnameStore) {
    companion object {
        private const val TAG = "SurnameLoader"
    }

    fun loadData(context: Context) {
        val gson = Gson()

        try {
            loadSurnameMapping(context, gson)
            loadSurnameHanjaMapping(context, gson)
            loadChosungMapping(context, gson)
            loadCharTripleDict(context, gson)
            Log.d(TAG, "=== SurnameData 로드 완료 ===")
        } catch (e: Exception) {
            Log.e(TAG, "데이터 로드 실패", e)
            throw e
        }
    }

    private fun loadSurnameMapping(context: Context, gson: Gson) {
        context.assets.open("surname/surname_mapping.json").use { stream ->
            BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                val type = object : TypeToken<Map<String, List<String>>>() {}.type
                store.surnameMapping = gson.fromJson(reader, type) ?: emptyMap()
                Log.d(TAG, "surnameMapping loaded: ${store.surnameMapping.size} entries")
            }
        }
    }

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
                val type = object : TypeToken<Map<String, CharTripleInfo>>() {}.type
                store.charTripleDict = gson.fromJson(reader, type) ?: emptyMap()
                Log.d(TAG, "charTripleDict loaded: ${store.charTripleDict.size} entries")
            }
        }
    }
}