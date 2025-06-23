// model/infrastructure/repository/impl/JsonSajuRepository.kt
package com.ssc.namespring.model.infrastructure.repository.impl

import android.content.Context
import com.ssc.namespring.model.infrastructure.data.YMDRecord
import com.ssc.namespring.model.infrastructure.repository.SajuRepository
import com.ssc.namespring.model.infrastructure.util.JsonAssetLoader

class JsonSajuRepository(context: Context) : SajuRepository {

    private val jsonLoader = JsonAssetLoader(context)
    private lateinit var ymdData: List<YMDRecord>

    init {
        loadData()
    }

    private fun loadData() {
        ymdData = loadYmdData()
    }

    private fun loadYmdData(): List<YMDRecord> {
        val jsonArray = jsonLoader.loadJsonArray("ymd_data.json")
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

    override fun findByDate(year: Int, month: Int, day: Int): YMDRecord? {
        return ymdData.find { it.year == year && it.month == month && it.day == day }
    }
}