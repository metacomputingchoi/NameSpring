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
    private val profileLoader = NameDataProfileLoader(stateManager, nameDataService)
    private val characterManager = NameDataCharacterManager(stateManager, updateService)
    private val dataProvider = NameDataProvider(stateManager)
    private val givenNameInfoFactory = GivenNameInfoFactory()

    fun initialize() {
        Log.d(TAG, "initialize()")
        nameComposition = NameComposition()
        stateManager.reset()
    }

    fun loadFromProfile(profile: Profile) {
        Log.d(TAG, "loadFromProfile: profile=${profile.profileName}")
        nameComposition = NameComposition.fromGivenNameInfo(profile.givenName)
        stateManager.reset()
        profileLoader.loadCharInfoFromProfile(profile, nameComposition)
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
        Log.d(TAG, "setCharData at $position: korean='$korean', hanja='$hanja'")
        nameComposition = characterManager.updateCharacterData(
            nameComposition, position, korean, hanja
        )
    }

    fun setHanjaInfo(position: Int, info: CharTripleInfo) {
        nameComposition = characterManager.updateCharacterWithHanjaInfo(
            nameComposition, position, info
        )
    }

    fun removeHanjaInfo(position: Int) {
        nameComposition = characterManager.removeHanjaInfo(nameComposition, position)
    }

    fun getCharDataList(): List<NameCharData> =
        dataProvider.getCharDataList(nameComposition)

    fun getCharData(position: Int): NameCharData? =
        dataProvider.getCharData(nameComposition, position)

    fun getHanjaInfo(position: Int): CharTripleInfo? =
        stateManager.getHanjaInfo(position)

    fun createGivenNameInfo(): GivenNameInfo? =
        givenNameInfoFactory.create(nameComposition, stateManager, ::getCharDataList)

    fun reset() {
        Log.d(TAG, "reset()")
        initialize()
    }
}