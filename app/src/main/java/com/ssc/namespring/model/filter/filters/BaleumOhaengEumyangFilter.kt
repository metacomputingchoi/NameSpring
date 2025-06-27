// model/filter/filters/BaleumOhaengEumyangFilter.kt
package com.ssc.namespring.model.filter.filters

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.FilterConstants
import com.ssc.namespring.model.filter.core.AbstractNameFilter
import com.ssc.namespring.model.filter.extractors.BaleumDataExtractor
import com.ssc.namespring.model.filter.extractors.BaleumDataValidator
import com.ssc.namespring.model.filter.utils.FilterValidationHelper
import com.ssc.namespring.model.service.EumYangAnalysisService

class BaleumOhaengEumyangFilter(
    private val getBaleumOhaeng: (Char) -> String?,
    private val getBaleumEumyang: (Char) -> Int?,
    private val checkBaleumOhaengHarmony: (String) -> Boolean
) : AbstractNameFilter() {

    private val eumYangAnalysisService = EumYangAnalysisService()
    private val dataExtractor = BaleumDataExtractor(getBaleumOhaeng, getBaleumEumyang)
    private val dataValidator = BaleumDataValidator(eumYangAnalysisService, checkBaleumOhaengHarmony)

    override fun getName(): String = FilterConstants.BALEUM_OHAENG_EUMYANG_FILTER

    override fun getValidationDetails(
        name: GeneratedName,
        context: FilterContext
    ): ValidationResult {

        val baleumData = dataExtractor.extract(name, context)
        val details = FilterValidationHelper.createDetails(
            "발음오행" to baleumData.combinedBaleumOhaeng,
            "발음음양" to baleumData.combinedEumyang,
            "총글자수" to (context.surLength + context.nameLength)
        )

        return dataValidator.validate(baleumData, context, details)
    }
}