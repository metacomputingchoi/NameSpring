// model/domain/service/name/NameDataCharacterManager.kt
package com.ssc.namespring.model.domain.service.name

import android.util.Log
import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.data.mapper.CharTripleInfo

class NameDataCharacterManager(
    private val stateManager: NameCompositionStateManager,
    private val updateService: NameCharacterUpdateService
) {
    companion object {
        private const val TAG = "NameDataCharacterManager"
    }

    fun updateCharacterData(
        composition: NameComposition,
        position: Int,
        korean: String,
        hanja: String
    ): NameComposition {
        val updated = updateService.updateCharacterData(composition, position, korean, hanja)
        stateManager.updateComposition(updated)
        return updated
    }

    fun updateCharacterWithHanjaInfo(
        composition: NameComposition,
        position: Int,
        info: CharTripleInfo
    ): NameComposition {
        val updated = updateService.updateCharacterWithHanjaInfo(composition, position, info)
        stateManager.updateComposition(updated)
        return updated
    }

    fun removeHanjaInfo(composition: NameComposition, position: Int): NameComposition {
        Log.d(TAG, "removeHanjaInfo: position=$position")
        stateManager.removeHanjaInfo(position)
        return composition.updateCharacter(position) { character ->
            character.clearHanja()
        }
    }
}