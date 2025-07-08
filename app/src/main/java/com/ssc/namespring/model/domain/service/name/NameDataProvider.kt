// model/domain/service/name/NameDataProvider.kt
package com.ssc.namespring.model.domain.service.name

import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.presentation.components.NameCharData

class NameDataProvider(
    private val stateManager: NameCompositionStateManager
) {
    fun getCharDataList(nameComposition: NameComposition): List<NameCharData> {
        return (0 until nameComposition.visibleCount).map { index ->
            val character = nameComposition.getCharacter(index)
            val stateData = stateManager.getState().currentStateMap[index]

            // stateManager의 현재 상태를 우선적으로 사용
            NameCharData(
                korean = stateData?.first ?: character?.korean ?: "",
                hanja = stateData?.second ?: character?.hanja ?: ""
            )
        }
    }

    fun getCharData(nameComposition: NameComposition, position: Int): NameCharData? {
        val stateData = stateManager.getState().currentStateMap[position]
        val character = nameComposition.getCharacter(position)

        return if (stateData != null) {
            // stateManager의 현재 상태를 우선적으로 사용
            NameCharData(
                korean = stateData.first,
                hanja = stateData.second
            )
        } else {
            character?.let {
                NameCharData(
                    korean = it.korean,
                    hanja = it.hanja
                )
            }
        }
    }
}