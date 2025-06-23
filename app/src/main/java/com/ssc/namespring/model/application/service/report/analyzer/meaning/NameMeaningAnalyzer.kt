// model/application/service/report/analyzer/meaning/NameMeaningAnalyzer.kt
package com.ssc.namespring.model.application.service.report.analyzer.meaning

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class NameMeaningAnalyzer {
    private val hanjaMeaningsData = ReportDataHolder.hanjaMeaningsData
    private val strings = ReportDataHolder.characterMeaningStrings

    fun analyzeMeaningScore(result: Name): Int {
        var score = strings.basicScore // 기본 점수

        // 한자 의미의 긍정성
        if (hasPositiveMeaning(result.hanja1Info) && hasPositiveMeaning(result.hanja2Info)) {
            score += strings.positiveMeaningBonus
        }

        // 의미의 조화
        if (hasMeaningHarmony(result)) {
            score += strings.harmonyBonus
        }

        return score.coerceIn(strings.scoreMin, strings.scoreMax)
    }

    private fun hasPositiveMeaning(hanja: com.ssc.namespring.model.domain.hanja.entity.Hanja): Boolean {
        val meaning = hanja.inmyeongYongDdeut ?: return false
        return hanjaMeaningsData.positiveMeanings.any { meaning.contains(it) }
    }

    private fun hasMeaningHarmony(result: Name): Boolean {
        val meaning1 = result.hanja1Info.inmyeongYongDdeut ?: ""
        val meaning2 = result.hanja2Info.inmyeongYongDdeut ?: ""

        // 의미가 서로 보완적이거나 시너지를 만드는지 확인
        for ((pattern, isHarmony) in hanjaMeaningsData.meaningHarmonyPatterns) {
            val keywords = pattern.split(strings.patternDelimiter)
            if (keywords.size >= strings.harmonyPatternSize &&
                meaning1.contains(keywords[0]) && meaning2.contains(keywords[1]) &&
                isHarmony) {
                return true
            }
        }

        return false
    }
}