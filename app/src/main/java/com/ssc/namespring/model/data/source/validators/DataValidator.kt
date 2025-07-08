// model/data/source/validators/DataValidator.kt
package com.ssc.namespring.model.data.source.validators

import com.ssc.namespring.model.domain.entity.SurnameData
import com.ssc.namespring.model.domain.entity.ValidationResult
import com.ssc.namespring.model.domain.service.interfaces.INameDataService

class DataValidator(
    private val nameDataService: INameDataService
) {
    fun validateAllData(): ValidationResult {
        val warnings = mutableListOf<String>()
        val criticalErrors = mutableListOf<String>()

        val nameValidation = nameDataService.validateData()
        warnings.addAll(nameValidation.warnings)
        criticalErrors.addAll(nameValidation.criticalErrors)

        val surnameValidation = SurnameData.validateData()
        warnings.addAll(surnameValidation.warnings)

        return ValidationResult(
            isValid = criticalErrors.isEmpty(),
            warnings = warnings,
            criticalErrors = criticalErrors
        )
    }
}