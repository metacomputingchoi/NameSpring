// model/filter/BaleumOhaengEumyangFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.service.EumYangAnalysisService
import com.ssc.namespring.model.util.ValidationResultFactory

class BaleumOhaengEumyangFilter(
    private val getBaleumOhaeng: (Char) -> String?,
    private val getBaleumEumyang: (Char) -> Int?,
    private val checkBaleumOhaengHarmony: (String) -> Boolean
) : AbstractNameFilter() {

    private val eumYangAnalysisService = EumYangAnalysisService()

    override fun getFilterName(): String = FilterConstants.BALEUM_OHAENG_EUMYANG_FILTER

    override fun getValidationDetails(
        name: GeneratedName,
        context: FilterContext
    ): ValidationResult {
        val surBaleumOhaeng = context.surHangul.mapNotNull { getBaleumOhaeng(it) }
        val surBaleumEumyang = context.surHangul.mapNotNull { getBaleumEumyang(it) }
        val nameBaleumOhaeng = name.combinedPronounciation.mapNotNull { getBaleumOhaeng(it) }
        val nameBaleumEumyang = name.combinedPronounciation.mapNotNull { getBaleumEumyang(it) }

        val combinedBaleumOhaeng = (surBaleumOhaeng + nameBaleumOhaeng).joinToString("")
        val combinedEumyang = (surBaleumEumyang + nameBaleumEumyang).joinToString("") { it.toString() }
        val totalChars = context.surLength + context.nameLength

        val details = FilterValidationHelper.createDetails(
            "발음오행" to combinedBaleumOhaeng,
            "발음음양" to combinedEumyang,
            "총글자수" to totalChars
        )

        // 데이터 완전성 체크
        if (combinedBaleumOhaeng.length != totalChars || combinedEumyang.length != totalChars) {
            return ValidationResultFactory.createFailure(
                "발음오행 또는 음양 정보가 누락됨",
                *details.toList().toTypedArray()
            )
        }

        // 음양 균형 체크
        val eumYangResult = eumYangAnalysisService.checkEumYangBalanceWithDetails(
            combinedEumyang, context.surLength, context.nameLength
        )
        details["음양균형"] = eumYangResult.details

        if (!eumYangResult.isValid) {
            return ValidationResult(
                isValid = false,
                reason = eumYangResult.reason,
                details = details
            )
        }

        // 오행 조화 체크
        val harmonyValid = checkBaleumOhaengHarmony(combinedBaleumOhaeng)
        details["오행조화"] = if (harmonyValid) "조화로움" else "부조화"

        return ValidationResultFactory.createConditional(
            harmonyValid,
            "발음오행이 조화로움",
            "발음오행이 상극 관계",
            *details.toList().toTypedArray()
        )
    }
}