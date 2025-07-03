// model/data/SajuInfo.kt
package com.ssc.namespring.model.data

import com.ssc.namingengine.data.analysis.NameAnalysisInfo
import java.io.Serializable
import java.time.LocalTime
import java.time.LocalDateTime

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
            val sajuAnalysisInfo = analysisInfo.sajuInfo
            val fourPillarsList = sajuAnalysisInfo.fourPillars.toList()

            return SajuInfo(
                fourPillars = fourPillarsList,
                yearPillar = Pillar.fromPillarString(fourPillarsList[0]),
                monthPillar = Pillar.fromPillarString(fourPillarsList[1]),
                dayPillar = Pillar.fromPillarString(fourPillarsList[2]),
                hourPillar = Pillar.fromPillarString(fourPillarsList[3]),
                sajuOhaengCount = sajuAnalysisInfo.sajuOhaengCount,
                missingElements = sajuAnalysisInfo.missingElements,
                dominantElements = sajuAnalysisInfo.dominantElements,
                elementBalance = sajuAnalysisInfo.elementBalance
            )
        }

        fun getTimeSlotName(hour: Int): String {
            return when (hour) {
                23, 0 -> "자시(子時)"
                1, 2 -> "축시(丑時)"
                3, 4 -> "인시(寅時)"
                5, 6 -> "묘시(卯時)"
                7, 8 -> "진시(辰時)"
                9, 10 -> "사시(巳時)"
                11, 12 -> "오시(午時)"
                13, 14 -> "미시(未時)"
                15, 16 -> "신시(申時)"
                17, 18 -> "유시(酉時)"
                19, 20 -> "술시(戌時)"
                21, 22 -> "해시(亥時)"
                else -> ""
            }
        }
    }
}

data class Pillar(
    val heavenlyStem: String,
    val earthlyBranch: String,
    val stemOhaeng: String,
    val branchOhaeng: String
) : Serializable {
    companion object {
        private val HEAVENLY_STEM_OHAENG = mapOf(
            "甲" to "木", "乙" to "木",
            "丙" to "火", "丁" to "火",
            "戊" to "土", "己" to "土",
            "庚" to "金", "辛" to "金",
            "壬" to "水", "癸" to "水"
        )

        private val EARTHLY_BRANCH_OHAENG = mapOf(
            "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
            "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
            "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
        )

        private val HEAVENLY_STEM_EUMYANG = mapOf(
            "甲" to 1, "乙" to 0,
            "丙" to 1, "丁" to 0,
            "戊" to 1, "己" to 0,
            "庚" to 1, "辛" to 0,
            "壬" to 1, "癸" to 0
        )

        private val EARTHLY_BRANCH_EUMYANG = mapOf(
            "子" to 1, "丑" to 0, "寅" to 1, "卯" to 0,
            "辰" to 1, "巳" to 0, "午" to 1, "未" to 0,
            "申" to 1, "酉" to 0, "戌" to 1, "亥" to 0
        )

        fun fromPillarString(pillar: String): Pillar {
            require(pillar.length == 2) { "간지는 2글자여야 합니다: $pillar" }

            val heavenlyStem = pillar[0].toString()
            val earthlyBranch = pillar[1].toString()

            val stemOhaeng = HEAVENLY_STEM_OHAENG[heavenlyStem]
                ?: throw IllegalArgumentException("알 수 없는 천간: $heavenlyStem")
            val branchOhaeng = EARTHLY_BRANCH_OHAENG[earthlyBranch]
                ?: throw IllegalArgumentException("알 수 없는 지지: $earthlyBranch")

            return Pillar(heavenlyStem, earthlyBranch, stemOhaeng, branchOhaeng)
        }

        fun getEumyang(pillar: String): Pair<Int, Int> {
            require(pillar.length == 2) { "간지는 2글자여야 합니다: $pillar" }

            val heavenlyStem = pillar[0].toString()
            val earthlyBranch = pillar[1].toString()

            val stemEumyang = HEAVENLY_STEM_EUMYANG[heavenlyStem] ?: 0
            val branchEumyang = EARTHLY_BRANCH_EUMYANG[earthlyBranch] ?: 0

            return stemEumyang to branchEumyang
        }
    }

    fun toPillarString(): String = heavenlyStem + earthlyBranch

    fun getPrimaryOhaeng(): String = stemOhaeng

    fun getAllOhaeng(): List<String> = listOfNotNull(
        stemOhaeng.takeIf { it.isNotEmpty() },
        branchOhaeng.takeIf { it.isNotEmpty() && it != stemOhaeng }
    )

    fun getEumyang(): Pair<Int, Int> = Companion.getEumyang(toPillarString())

    fun getDescription(): String {
        val (stemEumyang, branchEumyang) = getEumyang()
        val stemEumyangStr = if (stemEumyang == 1) "양" else "음"
        val branchEumyangStr = if (branchEumyang == 1) "양" else "음"

        return "$heavenlyStem($stemOhaeng-$stemEumyangStr)$earthlyBranch($branchOhaeng-$branchEumyangStr)"
    }
}
