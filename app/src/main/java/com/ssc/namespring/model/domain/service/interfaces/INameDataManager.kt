// model/domain/service/interfaces/INameDataManager.kt
package com.ssc.namespring.model.domain.service.interfaces

import com.ssc.namespring.model.presentation.components.NameCharData
import com.ssc.namespring.model.domain.entity.CharInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.data.mapper.CharTripleInfo

interface INameDataManager {
    fun initialize()
    fun loadFromProfile(profile: Profile)
    fun canAddChar(): Boolean
    fun canRemoveChar(): Boolean
    fun addChar()
    fun removeChar()
    fun setCharData(position: Int, korean: String, hanja: String)
    fun setHanjaInfo(position: Int, info: CharTripleInfo)
    fun removeHanjaInfo(position: Int)
    fun getCharCount(): Int
    fun getCharDataList(): List<NameCharData>
    fun getCharData(position: Int): NameCharData?
    fun getHanjaInfo(position: Int): CharTripleInfo?
    fun createGivenNameInfo(): GivenNameInfo?
    fun reset()
}