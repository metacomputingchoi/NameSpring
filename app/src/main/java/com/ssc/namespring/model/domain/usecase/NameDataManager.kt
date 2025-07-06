// model/domain/usecase/NameDataManager.kt
package com.ssc.namespring.model.domain.usecase

import com.ssc.namespring.model.presentation.components.NameCharData
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.service.name.NameDataService
import com.ssc.namespring.model.domain.service.name.NameCompositionStateManager
import com.ssc.namespring.model.domain.service.name.NameCharacterUpdateService
import com.ssc.namespring.model.domain.service.name.NameCompositionService
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory

class NameDataManager : INameDataManager {

    private val nameDataService: INameDataService = NameDataServiceFactory.getInstance()
    private val stateManager = NameCompositionStateManager()
    private val updateService = NameCharacterUpdateService(stateManager, NameCompositionService())
    private val dataService = NameDataService(stateManager, updateService, nameDataService)

    override fun initialize() {
        dataService.initialize()
    }

    override fun loadFromProfile(profile: Profile) {
        dataService.loadFromProfile(profile)
    }

    override fun canAddChar(): Boolean = dataService.canAddChar()

    override fun canRemoveChar(): Boolean = dataService.canRemoveChar()

    override fun addChar() {
        dataService.addChar()
    }

    override fun removeChar() {
        dataService.removeChar()
    }

    override fun setCharData(position: Int, korean: String, hanja: String) {
        dataService.setCharData(position, korean, hanja)
    }

    override fun setHanjaInfo(position: Int, info: CharTripleInfo) {
        dataService.setHanjaInfo(position, info)
    }

    override fun removeHanjaInfo(position: Int) {
        dataService.removeHanjaInfo(position)
    }

    override fun getCharCount(): Int = dataService.getCharCount()

    override fun getCharDataList(): List<NameCharData> = dataService.getCharDataList()

    override fun getCharData(position: Int): NameCharData? = dataService.getCharData(position)

    override fun getHanjaInfo(position: Int): CharTripleInfo? = dataService.getHanjaInfo(position)

    override fun createGivenNameInfo(): GivenNameInfo? = dataService.createGivenNameInfo()

    override fun reset() {
        dataService.reset()
    }
}