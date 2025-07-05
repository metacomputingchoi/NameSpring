// model/domain/entity/SajuInfo.kt
package com.ssc.namespring.model.domain.entity

import com.ssc.namespring.model.domain.service.SajuFactory
import com.ssc.namespring.model.data.mapper.TimeSlotMapper
import com.ssc.namingengine.data.analysis.NameAnalysisInfo
import java.io.Serializable
import java.time.LocalDateTime
import java.time.LocalTime

data class SajuInfo(
    val fourPillars: List<String>,
    val yearPillar: Pillar,
    val monthPillar: Pillar,
    val dayPillar: Pillar,
    val hourPillar: Pillar,
    val sajuOhaengCount: Map<String, Int>,
    val missingElements: List<String>,
    val dominantElements: List<String>,
    val elementBalance: Map<String, Float>
) : Serializable {

    fun getWeakestElement(): String? =
        sajuOhaengCount.minByOrNull { it.value }?.key

    fun getStrongestElement(): String? =
        sajuOhaengCount.maxByOrNull { it.value }?.key

    fun isBalanced(): Boolean =
        missingElements.isEmpty() && dominantElements.isEmpty()

    fun getElementDescription(): String {
        return when {
            missingElements.isNotEmpty() && dominantElements.isNotEmpty() ->
                "${dominantElements.joinToString(", ")}이(가) 많고, ${missingElements.joinToString(", ")}이(가) 부족합니다."
            missingElements.isNotEmpty() ->
                "${missingElements.joinToString(", ")}이(가) 부족합니다."
            dominantElements.isNotEmpty() ->
                "${dominantElements.joinToString(", ")}이(가) 많습니다."
            else -> "오행이 균형잡혀 있습니다."
        }
    }

    fun getElementBalancePercentage(): Map<String, Int> {
        return elementBalance.mapValues { (_, value) ->
            (value * 100).toInt()
        }
    }

    fun getDetailedDescription(): String {
        return buildString {
            append("사주팔자: ${fourPillars.joinToString(" ")}\n")
            append("오행 분포: ")
            OHAENG_ORDER.forEach { element ->
                val count = sajuOhaengCount[element] ?: 0
                append("$element($count) ")
            }
            append("\n")
            append(getElementDescription())
        }
    }

    fun getMostNeededElements(): List<String> {
        return if (missingElements.isNotEmpty()) {
            missingElements
        } else {
            val minCount = sajuOhaengCount.values.minOrNull() ?: 0
            sajuOhaengCount.filter { it.value == minCount }.keys.toList()
        }
    }

    fun needsYajasiAdjustment(birthTime: LocalDateTime): Boolean {
        return birthTime.toLocalTime() >= YAJASI_START_TIME
    }

    companion object {
        private val OHAENG_ORDER = listOf("木", "火", "土", "金", "水")
        val YAJASI_START_TIME = LocalTime.of(23, 30)

        fun fromAnalysisInfo(analysisInfo: NameAnalysisInfo): SajuInfo {
            return SajuFactory.createFromAnalysisInfo(analysisInfo)
        }

        fun getTimeSlotName(hour: Int): String {
            return TimeSlotMapper.getTimeSlotName(hour)
        }
    }
}