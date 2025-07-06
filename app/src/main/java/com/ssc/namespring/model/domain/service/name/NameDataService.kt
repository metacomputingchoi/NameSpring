// model/domain/service/name/NameDataService.kt
package com.ssc.namespring.model.domain.service.name

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.presentation.components.NameCharData
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.domain.service.interfaces.INameDataService

class NameDataService(
    private val stateManager: NameCompositionStateManager,
    private val updateService: NameCharacterUpdateService,
    private val nameDataService: INameDataService
) {
    companion object {
        private const val TAG = "NameDataService"
    }

    private var nameComposition = NameComposition()

    fun initialize() {
        Log.d(TAG, "initialize()")
        nameComposition = NameComposition()
        stateManager.reset()
    }

    fun loadFromProfile(profile: Profile) {
        Log.d(TAG, "loadFromProfile: profile=${profile.profileName}")

        nameComposition = NameComposition.fromGivenNameInfo(profile.givenName)
        stateManager.reset()

        // 프로필에서 한자 정보 복원
        profile.givenName?.charInfos?.forEachIndexed { index, charInfo ->
            if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                // 현재 상태 저장
                stateManager.updateCurrentState(index, charInfo.korean, charInfo.hanja)

                // CharTripleInfo 복원 시도
                nameDataService.getCharInfo(charInfo.korean, charInfo.hanja)?.let { info ->
                    stateManager.addHanjaInfo(index, info)
                    Log.d(TAG, "Loaded hanja info for position $index: ${charInfo.korean}/${charInfo.hanja}")
                }
            }
        }
    }

    fun canAddChar(): Boolean = nameComposition.canAddCharacter()
    fun canRemoveChar(): Boolean = nameComposition.canRemoveCharacter()
    fun getCharCount(): Int = nameComposition.size

    fun addChar() {
        Log.d(TAG, "addChar()")
        nameComposition = nameComposition.addCharacter()
    }

    fun removeChar() {
        Log.d(TAG, "removeChar()")
        val lastIndex = nameComposition.visibleCount - 1
        stateManager.clearPositionData(lastIndex)
        nameComposition = nameComposition.removeCharacter()
    }

    fun setCharData(position: Int, korean: String, hanja: String) {
        nameComposition = updateService.updateCharacterData(
            nameComposition,
            position,
            korean,
            hanja
        )
        stateManager.updateComposition(nameComposition)
    }

    fun setHanjaInfo(position: Int, info: CharTripleInfo) {
        nameComposition = updateService.updateCharacterWithHanjaInfo(
            nameComposition,
            position,
            info
        )
        stateManager.updateComposition(nameComposition)
    }

    fun removeHanjaInfo(position: Int) {
        Log.d(TAG, "removeHanjaInfo: position=$position")
        stateManager.removeHanjaInfo(position)
        nameComposition = nameComposition.updateCharacter(position) { character ->
            character.clearHanja()
        }
    }

    fun getCharDataList(): List<NameCharData> {
        return (0 until nameComposition.visibleCount).map { index ->
            val character = nameComposition.getCharacter(index)
            NameCharData(
                korean = character?.korean ?: "",
                hanja = character?.hanja ?: ""
            )
        }
    }

    fun getCharData(position: Int): NameCharData? {
        return nameComposition.getCharacter(position)?.let { character ->
            NameCharData(
                korean = character.korean,
                hanja = character.hanja
            )
        }
    }

    fun getHanjaInfo(position: Int): CharTripleInfo? = stateManager.getHanjaInfo(position)

    fun createGivenNameInfo(): GivenNameInfo? {
        Log.d(TAG, "createGivenNameInfo called")

        // 현재 상태 확인
        Log.d(TAG, "Current state:")
        val state = stateManager.getState()
        state.currentStateMap.forEach { (pos, stateData) ->
            Log.d(TAG, "  Position $pos: korean='${stateData.first}', hanja='${stateData.second}'")
        }

        val givenNameInfo = nameComposition.toGivenNameInfo()

        if (givenNameInfo != null) {
            Log.d(TAG, "Created GivenNameInfo: korean='${givenNameInfo.korean}', hanja='${givenNameInfo.hanja}'")
            givenNameInfo.charInfos.forEachIndexed { index, charInfo ->
                Log.d(TAG, "  CharInfo[$index]: korean='${charInfo.korean}', hanja='${charInfo.hanja}', " +
                        "meaning='${charInfo.meaning}', strokes=${charInfo.strokes}, " +
                        "ohaeng='${charInfo.ohaeng}', eumyang=${charInfo.eumyang}")
            }
        } else {
            Log.d(TAG, "GivenNameInfo is null")
        }

        return givenNameInfo
    }

    fun reset() {
        Log.d(TAG, "reset()")
        initialize()
    }
}