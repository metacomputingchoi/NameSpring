// model/data/SajuInfoExtensions.kt
package com.ssc.namespring.model.data

import com.ssc.namingengine.data.analysis.NameAnalysisInfo
import java.time.LocalDateTime

// Extension functions for backward compatibility
fun SajuInfo.Companion.fromAnalysisInfo(analysisInfo: NameAnalysisInfo): SajuInfo =
    SajuUtils.fromAnalysisInfo(analysisInfo)

fun SajuInfo.Companion.getTimeSlotName(hour: Int): String =
    SajuUtils.getTimeSlotName(hour)

fun SajuInfo.needsYajasiAdjustment(birthTime: LocalDateTime): Boolean =
    SajuUtils.needsYajasiAdjustment(birthTime)

fun Pillar.Companion.fromPillarString(pillar: String): Pillar =
    PillarUtils.fromPillarString(pillar)

fun Pillar.Companion.getEumyang(pillar: String): Pair<Int, Int> =
    PillarUtils.getEumyang(pillar)