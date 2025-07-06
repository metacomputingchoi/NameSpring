// model/domain/usecase/profileform/ProfileFormStateManager.kt
package com.ssc.namespring.model.domain.usecase.profileform

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo

class ProfileFormStateManager {
    private var profileName: String = ""
    private var isYajaTime: Boolean = true
    private var selectedSurname: SurnameInfo? = null

    fun loadFromProfile(profile: Profile) {
        profileName = profile.profileName
        isYajaTime = profile.isYajaTime
        selectedSurname = profile.surname
    }

    fun setProfileName(name: String) {
        profileName = name
    }

    fun getProfileName(): String = profileName

    fun updateYajaTime(isChecked: Boolean) {
        isYajaTime = isChecked
    }

    fun isYajaTime(): Boolean = isYajaTime

    fun setSurname(surname: SurnameInfo?) {
        selectedSurname = surname
    }

    fun getSurname(): SurnameInfo? = selectedSurname

    fun reset() {
        profileName = ""
        isYajaTime = true
        selectedSurname = null
    }
}