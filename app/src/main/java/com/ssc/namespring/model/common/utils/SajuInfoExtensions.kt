// model/common/utils/SajuInfoExtensions.kt
package com.ssc.namespring.model.common.utils

import com.ssc.namespring.model.domain.entity.SajuInfo
import com.ssc.namingengine.data.analysis.NameAnalysisInfo
import java.time.LocalDateTime

fun SajuInfo.Companion.fromAnalysisInfo(analysisInfo: NameAnalysisInfo): SajuInfo =
    SajuUtils.fromAnalysisInfo(analysisInfo)

fun SajuInfo.Companion.getTimeSlotName(hour: Int): String =
    SajuUtils.getTimeSlotName(hour)

fun SajuInfo.needsYajasiAdjustment(birthTime: LocalDateTime): Boolean =
    SajuUtils.needsYajasiAdjustment(birthTime)