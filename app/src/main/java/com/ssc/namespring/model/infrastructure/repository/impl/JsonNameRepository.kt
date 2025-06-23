// model/infrastructure/repository/impl/JsonNameRepository.kt
package com.ssc.namespring.model.infrastructure.repository.impl

import android.content.Context
import com.ssc.namespring.model.infrastructure.repository.NameRepository
import com.ssc.namespring.model.infrastructure.util.JsonAssetLoader
import org.json.JSONObject

class JsonNameRepository(context: Context) : NameRepository {

    private val jsonLoader = JsonAssetLoader(context)
    private lateinit var hangulGivenNames: Map<String, JSONObject>
    private lateinit var surnameMapping: Map<String, List<String>>
    private lateinit var validHanjaSet: Set<String>
    private lateinit var hanjaToHangulSurname: Map<String, List<String>>
    private lateinit var hanja2Stroke: Map<String, Int>

    init {
        loadData()
    }

    private fun loadData() {
        hangulGivenNames = loadHangulGivenNames()
        surnameMapping = loadSurnameMapping()
        hanja2Stroke = loadHanja2Stroke()

        // 유효한 한자 세트 생성
        val hanjaArray = jsonLoader.loadJsonArray("df_name_hanja.json")
        validHanjaSet = (0 until hanjaArray.length())
            .map { hanjaArray.getJSONObject(it).optString("한자", "") }
            .filter { it.isNotEmpty() }
            .toSet()

        // 한자 -> 한글 성씨 매핑
        hanjaToHangulSurname = mutableMapOf<String, MutableList<String>>().apply {
            surnameMapping.forEach { (hangul, hanjaList) ->
                hanjaList.forEach { hanja ->
                    if (hanja in validHanjaSet || hanja in hanja2Stroke) {
                        getOrPut(hanja) { mutableListOf() }.add(hangul)
                    }
                }
            }
        }
    }

    private fun loadHangulGivenNames(): Map<String, JSONObject> {
        val jsonObject = jsonLoader.loadJsonObject("dict_hangul_given_names.json")
        val map = mutableMapOf<String, JSONObject>()

        jsonObject.keys().forEach { key ->
            val obj = JSONObject()
            obj.put("name", key)
            val value = jsonObject.get(key)
            obj.put("value", value.toString())
            map[key] = obj
        }
        return map
    }

    private fun loadSurnameMapping(): Map<String, List<String>> {
        val jsonObject = jsonLoader.loadJsonObject("surname_mapping.json")
        val map = mutableMapOf<String, List<String>>()
        jsonObject.keys().forEach { key ->
            val array = jsonObject.getJSONArray(key)
            map[key] = (0 until array.length()).map { array.getString(it) }
        }
        return map
    }

    private fun loadHanja2Stroke(): Map<String, Int> {
        val jsonObject = jsonLoader.loadJsonObject("dict_hanja2stroke.json")
        val map = mutableMapOf<String, Int>()
        jsonObject.keys().forEach { key ->
            map[key] = jsonObject.getInt(key)
        }
        return map
    }

    override fun existsHangulName(name: String): Boolean {
        return name in hangulGivenNames
    }

    override fun getHangulNameData(name: String): JSONObject {
        return hangulGivenNames[name] ?: JSONObject()
    }

    override fun getHangulSurnameFromHanja(hanjasSurname: String): String {
        val hangulList = hanjaToHangulSurname[hanjasSurname]
            ?: throw IllegalArgumentException("'$hanjasSurname' 한자에 대응하는 한글 성씨를 찾을 수 없습니다.")

        if (hangulList.size > 1) {
            println("경고: '$hanjasSurname'에 대응하는 한글 성씨가 여러 개 있습니다: ${hangulList.joinToString(", ")}")
            println("첫 번째 성씨 '${hangulList[0]}'을(를) 사용합니다.")
        }
        return hangulList[0]
    }
}