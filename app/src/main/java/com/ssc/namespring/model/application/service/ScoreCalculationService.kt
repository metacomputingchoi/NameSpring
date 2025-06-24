// model/application/service/ScoreCalculationService.kt
package com.ssc.namespring.model.application.service

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.NameScore
import com.ssc.namespring.model.common.constants.Constants

class ScoreCalculationService {

    fun calculateDetailedScore(result: Name): NameScore {
        var fourTypesLuck = 0
        var nameElementHarmony = 0
        var typeElementHarmony = 0
        var yinYangBalance = 0
        var sajuComplement = 0
        var pronunciation = 0
        var meaning = 0

        // 1. 사격 수리 길흉 점수
        val luckCount = result.combinationAnalysis.fourTypesLuck.sum()
        fourTypesLuck = when (luckCount) {
            Constants.SCORE_LUCK_PERFECT -> Constants.SCORING_RULES["four_types_luck"]!!["perfect"]!!
            Constants.SCORE_LUCK_THREE -> Constants.SCORING_RULES["four_types_luck"]!!["three"]!!
            Constants.SCORE_LUCK_TWO -> Constants.SCORING_RULES["four_types_luck"]!!["two"]!!
            Constants.SCORE_LUCK_ONE -> Constants.SCORING_RULES["four_types_luck"]!!["one"]!!
            else -> Constants.SCORING_RULES["four_types_luck"]!!["zero"]!!
        }

        // 2. 삼원오행 조화 점수
        val nameCoexist = result.combinationAnalysis.scoreCoexistName
        val hasNameConflict = result.combinationAnalysis.nameElementChecks.any { it.result == "conflicting" }

        nameElementHarmony = when {
            hasNameConflict -> Constants.SCORING_RULES["name_element_harmony"]!!["conflict"]!!
            nameCoexist == Constants.SCORE_NAME_COEXIST_PERFECT -> Constants.SCORING_RULES["name_element_harmony"]!!["perfect"]!!
            nameCoexist == Constants.SCORE_NAME_COEXIST_GOOD -> Constants.SCORING_RULES["name_element_harmony"]!!["good"]!!
            else -> Constants.SCORING_RULES["name_element_harmony"]!!["neutral"]!!
        }

        // 3. 사격오행 조화 점수
        val typeCoexist = result.combinationAnalysis.scoreCoexistType
        val hasTypeConflict = result.combinationAnalysis.typeElementChecks.any { it.result == "conflicting" }

        typeElementHarmony = when {
            hasTypeConflict -> Constants.SCORING_RULES["type_element_harmony"]!!["conflict"]!!
            typeCoexist == Constants.SCORE_TYPE_COEXIST_PERFECT -> Constants.SCORING_RULES["type_element_harmony"]!!["perfect"]!!
            typeCoexist == Constants.SCORE_TYPE_COEXIST_GOOD -> Constants.SCORING_RULES["type_element_harmony"]!!["good"]!!
            typeCoexist == Constants.SCORE_TYPE_COEXIST_NEUTRAL -> Constants.SCORING_RULES["type_element_harmony"]!!["neutral"]!!
            else -> Constants.SCORING_RULES["type_element_harmony"]!!["conflict"]!!
        }

        // 4. 음양 균형 점수
        val pmSet = result.combinedPm!!.toSet()
        yinYangBalance = when {
            pmSet.size == Constants.SCORE_YIN_YANG_SET_SIZE && result.combinedPm!![Constants.PM_FIRST_INDEX] != result.combinedPm!![Constants.PM_THIRD_INDEX] ->
                Constants.SCORING_RULES["yin_yang_balance"]!!["perfect"]!!
            pmSet.size == Constants.SCORE_YIN_YANG_SET_SIZE -> Constants.SCORING_RULES["yin_yang_balance"]!!["good"]!!
            else -> Constants.SCORING_RULES["yin_yang_balance"]!!["poor"]!!
        }

        // 5. 사주 보완 점수
        val jawonCheck = result.filteringProcess.find { it.step == "jawon_check" }
        if (jawonCheck != null && jawonCheck.passed) {
            val details = jawonCheck.details!!["details"] as Map<*, *>
            val checkType = details["check_type"] as String
            sajuComplement = when {
                "zero_element" in checkType -> Constants.SCORING_RULES["saju_complement"]!!["perfect"]!!
                "one_element" in checkType -> Constants.SCORING_RULES["saju_complement"]!!["good"]!!
                else -> Constants.SCORING_RULES["saju_complement"]!!["neutral"]!!
            }
        } else {
            sajuComplement = Constants.SCORING_RULES["saju_complement"]!!["poor"]!!
        }

        // 6. 발음 조화 점수
        val hangulCheck = result.filteringProcess.find { it.step == "hangul_naturalness_check" }
        pronunciation = if (hangulCheck != null && hangulCheck.passed) {
            Constants.SCORING_RULES["pronunciation"]!!["natural"]!!
        } else {
            Constants.SCORING_RULES["pronunciation"]!!["unnatural"]!!
        }

        // 7. 뜻의 조화 점수
        meaning = Constants.SCORING_RULES["meaning"]!!["neutral"]!!

        // 총점 계산
        val total = fourTypesLuck + nameElementHarmony + typeElementHarmony +
                yinYangBalance + sajuComplement + pronunciation + meaning

        return NameScore(
            fourTypesLuck, nameElementHarmony, typeElementHarmony,
            yinYangBalance, sajuComplement, pronunciation, meaning, total
        )
    }
}