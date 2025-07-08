// model/domain/validation/ProfileFormValidationHelper.kt
package com.ssc.namespring.model.domain.validation

import android.util.Log
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo

/**
 * ProfileForm의 유효성 검증을 담당하는 헬퍼 클래스
 */
class ProfileFormValidationHelper {

    companion object {
        private const val TAG = "ProfileFormValidation"
        private const val MIN_NAME_LENGTH = 1
        private const val MAX_NAME_LENGTH = 4
    }

    data class ProfileValidationResult(
        val isValid: Boolean,
        val message: String? = null
    )

    fun validateForNaming(
        surname: SurnameInfo?,
        nameCharCount: Int
    ): ProfileValidationResult {

        if (!isValidSurname(surname)) {
            val message = "작명 모드: 성씨 정보가 불완전합니다"
            Log.w(TAG, message)
            return ProfileValidationResult(false, message)
        }

        if (nameCharCount !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            val message = "작명 모드: 이름은 1~4글자여야 합니다"
            Log.w(TAG, message)
            return ProfileValidationResult(false, message)
        }

        return ProfileValidationResult(true)
    }

    fun validateForEvaluation(
        surname: SurnameInfo?,
        givenNameInfo: GivenNameInfo?
    ): ProfileValidationResult {

        if (!isValidSurname(surname)) {
            val message = "평가 모드: 성씨 정보가 불완전합니다"
            Log.w(TAG, message)
            return ProfileValidationResult(false, message)
        }

        if (givenNameInfo == null || givenNameInfo.charInfos.isEmpty()) {
            val message = "평가 모드: 이름 정보가 없습니다"
            Log.w(TAG, message)
            return ProfileValidationResult(false, message)
        }

        val nameCharCount = givenNameInfo.charInfos.size
        if (nameCharCount !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            val message = "평가 모드: 이름은 1~4글자여야 합니다"
            Log.w(TAG, message)
            return ProfileValidationResult(false, message)
        }

        val allFilled = givenNameInfo.charInfos.all { charInfo ->
            charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()
        }

        if (!allFilled) {
            val message = "평가 모드: 모든 이름의 한글과 한자가 입력되어야 합니다"
            Log.w(TAG, message)
            return ProfileValidationResult(false, message)
        }

        return ProfileValidationResult(true)
    }

    private fun isValidSurname(surname: SurnameInfo?): Boolean {
        return surname != null && 
               surname.korean.isNotEmpty() && 
               surname.hanja.isNotEmpty()
    }
}
