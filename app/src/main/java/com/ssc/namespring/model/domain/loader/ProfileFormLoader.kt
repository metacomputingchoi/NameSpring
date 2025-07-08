// model/domain/loader/ProfileFormLoader.kt
package com.ssc.namespring.model.domain.loader

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager

/**
 * Profile 데이터 로딩을 담당하는 헬퍼 클래스
 */
class ProfileFormLoader(
    private val dateTimeManager: ProfileFormDateTimeManager,
    private val nameDataManager: NameDataManager,
    private val stateManager: ProfileFormStateManager,
    private val nameDataService: INameDataService
) {
    companion object {
        private const val TAG = "ProfileFormLoader"
    }

    fun loadProfileData(profile: Profile) {
        stateManager.loadFromProfile(profile)
        dateTimeManager.setDateTime(profile.birthDate)
        nameDataManager.loadFromProfile(profile)
    }

    fun loadFromParentProfile(parentProfile: Profile): Boolean {
        val updates = mutableListOf<() -> Unit>()

        // 날짜/시간 정보 로드
        dateTimeManager.setDateTime(parentProfile.birthDate)
        stateManager.updateYajaTime(parentProfile.isYajaTime)

        // 성씨 정보 로드
        stateManager.setSurname(parentProfile.surname)

        // 이름 정보 로드
        parentProfile.givenName?.let { givenName ->
            nameDataManager.reset()

            // 글자 수 맞추기
            val targetCount = givenName.charInfos.size
            repeat(targetCount - 1) {
                nameDataManager.addChar()
            }

            // 모든 데이터 준비 - 빈 값도 허용
            givenName.charInfos.forEachIndexed { index, charInfo ->
                Log.d(TAG, "Loading char at $index: korean='${charInfo.korean}', hanja='${charInfo.hanja}'")
                updates.add {
                    // 빈 값이어도 로드
                    nameDataManager.setCharData(index, charInfo.korean, charInfo.hanja)

                    // 한글과 한자가 모두 있는 경우에만 charInfo 설정
                    if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                        nameDataService.getCharInfo(charInfo.korean, charInfo.hanja)?.let { info ->
                            nameDataManager.setHanjaInfo(index, info)
                        }
                    }
                }
            }
        }

        // 모든 업데이트 실행
        updates.forEach { it() }

        return true
    }
}