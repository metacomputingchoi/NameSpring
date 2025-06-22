// model/loader/DataLoader.kt
package com.ssc.namespring.model.loader

import android.content.Context
import com.ssc.namespring.model.data.*
import org.json.JSONArray
import org.json.JSONObject

class DataLoader(private val context: Context) {

    lateinit var ymdData: List<YMDRecord>
    lateinit var hanjaInfo: List<HanjaInfo>
    lateinit var hangulGivenNames: Map<String, JSONObject>
    lateinit var hanja2Stroke: Map<String, Int>
    lateinit var surnameMapping: Map<String, List<String>>
    lateinit var validHanjaSet: Set<String>
    lateinit var hanjaToHangulSurname: Map<String, List<String>>

    init {
        loadData()
    }

    private fun loadData() {
        ymdData = loadYmdData()
        hanjaInfo = loadHanjaInfo()
        hangulGivenNames = loadHangulGivenNames()
        hanja2Stroke = loadHanja2Stroke()
        surnameMapping = loadSurnameMapping()

        // 유효한 한자 세트 생성
        validHanjaSet = hanjaInfo.filter { it.hanja.isNotEmpty() }
            .map { it.hanja }.toSet()

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

    private fun loadYmdData(): List<YMDRecord> {
        val jsonString = context.assets.open("ymd_data.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        return (0 until jsonArray.length()).map { i ->
            val obj = jsonArray.getJSONObject(i)
            YMDRecord(
                obj.getInt("연"),
                obj.getInt("월"),
                obj.getInt("일"),
                obj.getString("연주"),
                obj.getString("월주"),
                obj.getString("일주")
            )
        }
    }

    private fun loadHanjaInfo(): List<HanjaInfo> {
        val jsonString = context.assets.open("df_name_hanja.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val hanjaList = mutableListOf<HanjaInfo>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            hanjaList.add(
                HanjaInfo(
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
        return hanjaList
    }

    private fun loadHangulGivenNames(): Map<String, JSONObject> {
        val jsonString = context.assets.open("dict_hangul_given_names.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
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

    private fun loadHanja2Stroke(): Map<String, Int> {
        val jsonString = context.assets.open("dict_hanja2stroke.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, Int>()
        jsonObject.keys().forEach { key ->
            map[key] = jsonObject.getInt(key)
        }
        return map
    }

    private fun loadSurnameMapping(): Map<String, List<String>> {
        val jsonString = context.assets.open("surname_mapping.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, List<String>>()
        jsonObject.keys().forEach { key ->
            val array = jsonObject.getJSONArray(key)
            map[key] = (0 until array.length()).map { array.getString(it) }
        }
        return map
    }

    fun getHangulSurnameFromHanja(hanjasSurname: String): String {
        val hangulList = hanjaToHangulSurname[hanjasSurname]
            ?: throw IllegalArgumentException("'$hanjasSurname' 한자에 대응하는 한글 성씨를 찾을 수 없습니다.")

        if (hangulList.size > 1) {
            println("경고: '$hanjasSurname'에 대응하는 한글 성씨가 여러 개 있습니다: ${hangulList.joinToString(", ")}")
            println("첫 번째 성씨 '${hangulList[0]}'을(를) 사용합니다.")
        }
        return hangulList[0]
    }
}
