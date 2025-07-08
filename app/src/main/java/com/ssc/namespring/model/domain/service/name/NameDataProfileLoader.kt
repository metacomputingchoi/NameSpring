// model/domain/service/name/NameDataProfileLoader.kt
package com.ssc.namespring.model.domain.service.name

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.domain.service.interfaces.INameDataService

class NameDataProfileLoader(
    private val stateManager: NameCompositionStateManager,
    private val nameDataService: INameDataService
) {
    companion object {
        private const val TAG = "NameDataProfileLoader"
    }

    private val validationHelper = NameCompositionValidationHelper()

    fun loadCharInfoFromProfile(profile: Profile, nameComposition: NameComposition) {
        profile.givenName?.charInfos?.forEachIndexed { index, charInfo ->
            // 현재 상태 저장 - 빈 값도 허용
            stateManager.updateCurrentState(index, charInfo.korean, charInfo.hanja)

            // ValidationHelper를 사용하여 완전한 한자 정보가 있는지 확인
            val character = nameComposition.getCharacter(index)
            if (validationHelper.hasCompleteHanjaInfo(character)) {
                nameDataService.getCharInfo(charInfo.korean, charInfo.hanja)?.let { info ->
                    stateManager.addHanjaInfo(index, info)
                    Log.d(TAG, "Loaded hanja info for position $index: ${charInfo.korean}/${charInfo.hanja}")
                }
            }
        }
    }
}