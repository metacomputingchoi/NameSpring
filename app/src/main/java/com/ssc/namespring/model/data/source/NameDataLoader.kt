// model/data/source/NameDataLoader.kt
package com.ssc.namespring.model.data.source

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.data.mapper.OptimizedMapping
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import java.io.BufferedReader
import java.io.InputStreamReader

class NameDataLoader {
    companion object {
        private const val TAG = "NameDataLoader"
    }

    private val gson = Gson()

    fun loadOptimizedMapping(context: Context): OptimizedMapping? {
        return try {
            context.assets.open("name/name_optimized_search_mapping.json").use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    gson.fromJson(reader, OptimizedMapping::class.java).also {
                        Log.d(TAG, "최적화된 매핑 로드 완료")
                        Log.d(TAG, "통계: ${it.stats}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "최적화된 매핑 로드 실패", e)
            null
        }
    }

    fun loadCharTripleDict(context: Context): Map<String, CharTripleInfo> {
        return try {
            context.assets.open("name/name_char_triple_dict_effective.json").use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    val type = object : TypeToken<Map<String, CharTripleInfo>>() {}.type
                    val dict: Map<String, CharTripleInfo> = gson.fromJson(reader, type)
                    Log.d(TAG, "트리플 딕셔너리 로드 완료: ${dict.size}개")
                    dict
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "트리플 딕셔너리 로드 실패", e)
            emptyMap()
        }
    }
}