// model/filter/filters/BaleumOhaengEumyangFilter.kt
package com.ssc.namingengine.filter.filters

import com.ssc.namingengine.data.FilterContext
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.analysis.ValidationResult
import com.ssc.namingengine.filter.constants.FilterConstants
import com.ssc.namingengine.filter.core.AbstractNameFilter
import com.ssc.namingengine.filter.extractors.BaleumDataExtractor
import com.ssc.namingengine.filter.extractors.BaleumDataValidator
import com.ssc.namingengine.filter.utils.FilterValidationHelper
import com.ssc.namingengine.service.EumYangAnalysisService

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