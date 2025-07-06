// model/domain/entity/ProfileFormConfig.kt
package com.ssc.namespring.model.domain.entity

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class ProfileFormConfig(
    val mode: ProfileFormMode,
    val profileId: String? = null,
    val defaultProfileName: String? = null,  // 기본 프로필 이름 추가
    val parentProfileId: String? = null      // 작명/평가 시 원본 프로필 ID
) : Serializable {

    val titleText: String
        get() = when (mode) {
            ProfileFormMode.CREATE -> "새 프로필 만들기"
            ProfileFormMode.EDIT -> "기존 프로필 수정하기"
            ProfileFormMode.NAMING -> "작명하기"
            ProfileFormMode.EVALUATION -> "평가하기"
        }

    val profileLabelText: String
        get() = when (mode) {
            ProfileFormMode.NAMING, ProfileFormMode.EVALUATION -> "작업명"
            ProfileFormMode.CREATE, ProfileFormMode.EDIT -> "프로필 이름"
        }

    val showLoadButton: Boolean
        get() = mode in listOf(ProfileFormMode.NAMING, ProfileFormMode.EVALUATION)

    val profileLabelHint: String
        get() = when (mode) {
            ProfileFormMode.NAMING -> "예: 첫째 작명 ${getCurrentDateTime()}"
            ProfileFormMode.EVALUATION -> "예: 우리 아이 이름 평가"
            else -> "예: 첫째, 우리 아이"
        }

    val workDescription: String
        get() = when (mode) {
            ProfileFormMode.NAMING -> "작명 결과는 별도로 저장되지 않으며, 작업명은 구분용으로만 사용됩니다."
            ProfileFormMode.EVALUATION -> "평가 결과는 별도로 저장되지 않으며, 작업명은 구분용으로만 사용됩니다."
            else -> ""
        }

    val saveButtonText: String
        get() = when (mode) {
            ProfileFormMode.CREATE -> "프로필 생성"
            ProfileFormMode.EDIT -> "프로필 수정"
            ProfileFormMode.NAMING -> "이름 생성"
            ProfileFormMode.EVALUATION -> "이름 진단"
        }

    val successMessage: String
        get() = when (mode) {
            ProfileFormMode.CREATE -> "프로필이 생성되었습니다"
            ProfileFormMode.EDIT -> "프로필이 수정되었습니다"
            ProfileFormMode.NAMING -> "작명 프로필이 생성되었습니다"
            ProfileFormMode.EVALUATION -> "평가 프로필이 생성되었습니다"
        }

    val nameInputLabelText: String
        get() = when (mode) {
            ProfileFormMode.EVALUATION -> "이름 (필수사항)"
            else -> "이름 (선택사항)"
        }

    val shouldShowAllFields: Boolean
        get() = when (mode) {
            ProfileFormMode.NAMING, ProfileFormMode.EVALUATION -> true
            else -> true
        }

    fun getDefaultName(): String {
        return defaultProfileName ?: when (mode) {
            ProfileFormMode.NAMING -> "작명 - ${getCurrentDateTime()}"
            ProfileFormMode.EVALUATION -> "평가 - ${getCurrentDateTime()}"
            else -> ""
        }
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return formatter.format(Date())
    }
}

enum class ProfileFormMode {
    CREATE,
    EDIT,
    NAMING,      // 작명 모드 추가
    EVALUATION   // 평가 모드 추가
}