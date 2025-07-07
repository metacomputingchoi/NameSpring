// model/domain/usecase/profileform/ProfileFormValidator.kt
package com.ssc.namespring.model.domain.usecase.profileform

import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.validation.ProfileFormValidationHelper

internal class ProfileFormValidator {
    private val validationHelper = ProfileFormValidationHelper()

    fun isValidForNaming(surname: SurnameInfo?, nameCharCount: Int): Boolean {
        val result = validationHelper.validateForNaming(surname, nameCharCount)
        return result.isValid
    }

    fun isValidForEvaluation(surname: SurnameInfo?, givenNameInfo: GivenNameInfo?): Boolean {
        val result = validationHelper.validateForEvaluation(surname, givenNameInfo)
        return result.isValid
    }
}