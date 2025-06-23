// model/infrastructure/repository/impl/JsonHanjaRepository.kt
package com.ssc.namespring.model.infrastructure.repository.impl

import android.content.Context
import com.ssc.namespring.model.domain.hanja.entity.Hanja
import com.ssc.namespring.model.infrastructure.repository.HanjaRepository
import org.json.JSONArray
import org.json.JSONObject

class JsonHanjaRepository(private val context: Context) : HanjaRepository {

    private lateinit var hanjaList: List<Hanja>
    private lateinit var hanja2Stroke: Map<String, Int>

    init {
        loadData()
    }

    private fun loadData() {
        hanjaList = loadHanjaInfo()
        hanja2Stroke = loadHanja2Stroke()
    }

    private fun loadHanjaInfo(): List<Hanja> {
        val jsonString = context.assets.open("df_name_hanja.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<Hanja>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Hanja(
                    hanja = obj.optString("한자", ""),
                    inmyeongYongEum = obj.optString("인명용 음", null),
                    inmyeongYongDdeut = obj.optString("인명용 뜻", null),
                    wonHoeksu = obj.optInt("원획수", 0),
                    jawonOheng = obj.optString("자원오행", null),
                    baleumOheng = obj.optString("발음오행", null),
                    cautionRed = obj.optString("CAUTION_RED", null),
                    cautionBlue = obj.optString("CAUTION_BLUE", null)
                )
            )
        }
        return list
    }

    private fun loadHanja2Stroke(): Map<String, Int> {
        val jsonString = context.assets.open("dict_hanja2stroke.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, Int>()
        jsonObject.keys().forEach { key ->
            map[key] = jsonObject.getInt(key)
        }
        return map
    }

    override fun findByHanja(hanja: String): List<Hanja> {
        return hanjaList.filter { it.hanja == hanja }
    }

    override fun findByStroke(stroke: Int): List<Hanja> {
        return hanjaList.filter { it.wonHoeksu == stroke }
    }

    override fun findByStrokeAndPronunciation(stroke: Int, pronunciation: String): List<Hanja> {
        return hanjaList.filter { it.wonHoeksu == stroke && it.inmyeongYongEum == pronunciation }
    }

    override fun getStrokeMap(): Map<String, Int> {
        return hanja2Stroke
    }
}