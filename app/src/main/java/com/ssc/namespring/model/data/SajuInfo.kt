// model/data/SajuInfo.kt
package com.ssc.namespring.model.data

import java.time.LocalTime
import java.time.LocalDateTime

/**
 * 사주 정보를 담는 데이터 클래스
 * NamingEngine의 SajuAnalysisInfo에서 추출한 정보를 저장
 */
data class SajuInfo(
    val fourPillars: List<String>,           // 사주팔자 [연주, 월주, 일주, 시주]
    val yearPillar: Pillar,                  // 연주
    val monthPillar: Pillar,                 // 월주
    val dayPillar: Pillar,                   // 일주
    val hourPillar: Pillar,                  // 시주
    val sajuOhaengCount: Map<String, Int>,   // 오행별 개수 {木=2, 火=3, 土=1, 金=1, 水=1}
    val missingElements: List<String>,       // 부족한 오행
    val dominantElements: List<String>,      // 과다한 오행
    val elementBalance: Map<String, Float>   // 오행 균형 비율 (Float로 수정)
) {
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

    /**
     * 사주에서 가장 필요한 오행 계산
     */
    fun getMostNeededElements(): List<String> {
        return if (missingElements.isNotEmpty()) {
            missingElements
        } else {
            // 부족한 오행이 없다면 가장 적은 오행들 반환
            val minCount = sajuOhaengCount.values.minOrNull() ?: 0
            sajuOhaengCount.filter { it.value == minCount }.keys.toList()
        }
    }

    /**
     * 야자시 처리가 필요한지 확인
     */
    fun needsYajasiAdjustment(birthTime: LocalDateTime): Boolean {
        return birthTime.toLocalTime() >= YAJASI_START_TIME
    }

    /**
     * 야자시 적용된 일진 계산
     */
    fun getAdjustedDayPillar(birthDateTime: LocalDateTime, useYajasi: Boolean): Pillar {
        return if (useYajasi && needsYajasiAdjustment(birthDateTime)) {
            // 야자시 적용 시 다음날 일진으로 계산
            // 실제 구현은 NamingEngine에서 처리
            dayPillar // 임시로 원래 일진 반환
        } else {
            dayPillar
        }
    }

    companion object {
        // 오행 순서 (엔진의 OHAENG_SUNSE와 동일)
        private val OHAENG_ORDER = listOf("木", "火", "土", "金", "水")

        // 야자시 기준 시간 (엔진의 Yajasi.START_TIME과 동일)
        val YAJASI_START_TIME = LocalTime.of(23, 30)

        /**
         * GeneratedName의 analysisInfo에서 SajuInfo 추출
         */
        fun fromAnalysisInfo(analysisInfo: com.ssc.namingengine.data.analysis.NameAnalysisInfo): SajuInfo {
            val sajuAnalysisInfo = analysisInfo.sajuInfo
            val fourPillarsList = sajuAnalysisInfo.fourPillars.toList() // Array를 List로 변환

            // 사주를 파싱하여 Pillar 객체 생성
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

        /**
         * 시간대 이름 반환
         */
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

        /**
         * 12지지로 다음날 계산
         */
        fun getNextDayBranch(currentBranch: String): String {
            val branches = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
            val currentIndex = branches.indexOf(currentBranch)
            return if (currentIndex >= 0) {
                branches[(currentIndex + 1) % 12]
            } else {
                currentBranch
            }
        }
    }
}

data class Pillar(
    val heavenlyStem: String,      // 천간
    val earthlyBranch: String,     // 지지
    val stemOhaeng: String,        // 천간 오행
    val branchOhaeng: String       // 지지 오행
) {
    companion object {
        // 천간 오행 매핑 (엔진의 CHEONGAN_OHAENG과 동일)
        private val HEAVENLY_STEM_OHAENG = mapOf(
            "甲" to "木", "乙" to "木",
            "丙" to "火", "丁" to "火",
            "戊" to "土", "己" to "土",
            "庚" to "金", "辛" to "金",
            "壬" to "水", "癸" to "水"
        )

        // 지지 오행 매핑 (엔진의 JIJI_OHAENG과 동일)
        private val EARTHLY_BRANCH_OHAENG = mapOf(
            "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
            "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
            "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
        )

        // 천간 음양
        private val HEAVENLY_STEM_EUMYANG = mapOf(
            "甲" to 1, "乙" to 0,  // 목: 갑(양), 을(음)
            "丙" to 1, "丁" to 0,  // 화: 병(양), 정(음)
            "戊" to 1, "己" to 0,  // 토: 무(양), 기(음)
            "庚" to 1, "辛" to 0,  // 금: 경(양), 신(음)
            "壬" to 1, "癸" to 0   // 수: 임(양), 계(음)
        )

        // 지지 음양
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

        /**
         * 간지의 음양 판단
         */
        fun getEumyang(pillar: String): Pair<Int, Int> {
            require(pillar.length == 2) { "간지는 2글자여야 합니다: $pillar" }

            val heavenlyStem = pillar[0].toString()
            val earthlyBranch = pillar[1].toString()

            val stemEumyang = HEAVENLY_STEM_EUMYANG[heavenlyStem] ?: 0
            val branchEumyang = EARTHLY_BRANCH_EUMYANG[earthlyBranch] ?: 0

            return stemEumyang to branchEumyang
        }
    }

    /**
     * 간지 문자열로 변환
     */
    fun toPillarString(): String = heavenlyStem + earthlyBranch

    /**
     * 간지의 주요 오행 (천간 기준)
     */
    fun getPrimaryOhaeng(): String = stemOhaeng

    /**
     * 간지의 모든 오행
     */
    fun getAllOhaeng(): List<String> = listOfNotNull(
        stemOhaeng.takeIf { it.isNotEmpty() },
        branchOhaeng.takeIf { it.isNotEmpty() && it != stemOhaeng }
    )

    /**
     * 간지의 음양 값
     */
    fun getEumyang(): Pair<Int, Int> = Companion.getEumyang(toPillarString())

    /**
     * 간지 설명
     */
    fun getDescription(): String {
        val (stemEumyang, branchEumyang) = getEumyang()
        val stemEumyangStr = if (stemEumyang == 1) "양" else "음"
        val branchEumyangStr = if (branchEumyang == 1) "양" else "음"

        return "$heavenlyStem($stemOhaeng-$stemEumyangStr)$earthlyBranch($branchOhaeng-$branchEumyangStr)"
    }
}

/**
 * 오행 관계 분석 유틸리티
 */
object OhaengRelationAnalyzer {
    private val OHAENG_ORDER = listOf("木", "火", "土", "金", "水")

    /**
     * 두 오행이 상생 관계인지 확인
     * 木生火, 火生土, 土生金, 金生水, 水生木
     */
    fun isGenerating(from: String, to: String): Boolean {
        val fromIndex = OHAENG_ORDER.indexOf(from)
        val toIndex = OHAENG_ORDER.indexOf(to)

        if (fromIndex == -1 || toIndex == -1) return false

        return (fromIndex + 1) % 5 == toIndex
    }

    /**
     * 두 오행이 상극 관계인지 확인
     * 木克土, 土克水, 水克火, 火克金, 金克木
     */
    fun isConflicting(from: String, to: String): Boolean {
        val fromIndex = OHAENG_ORDER.indexOf(from)
        val toIndex = OHAENG_ORDER.indexOf(to)

        if (fromIndex == -1 || toIndex == -1) return false

        return (fromIndex + 2) % 5 == toIndex
    }

    /**
     * 오행 관계 설명
     */
    fun getRelationDescription(from: String, to: String): String {
        return when {
            isGenerating(from, to) -> "$from 生$to (상생)"
            isConflicting(from, to) -> "$from 克$to (상극)"
            from == to -> "같은 오행"
            else -> "무관"
        }
    }
}