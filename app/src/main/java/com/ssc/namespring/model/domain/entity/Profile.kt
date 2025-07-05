// model/domain/entity/Profile.kt
package com.ssc.namespring.model.domain.entity

import com.ssc.namespring.model.presentation.formatter.ProfileStringFormatter
import com.ssc.namespring.model.domain.service.utils.ProfileUpdater
import com.ssc.namingengine.data.GeneratedName
import java.io.Serializable
import java.util.Calendar

data class Profile(
    val id: String = System.currentTimeMillis().toString(),
    var profileName: String,
    var birthDate: Calendar,
    var isYajaTime: Boolean = false,
    var surname: SurnameInfo? = null,
    var givenName: GivenNameInfo? = null,
    var nameBomScore: Int = 0,
    var sajuInfo: SajuInfo? = null,
    var ohaengInfo: OhaengInfo? = null,
    var evaluatedName: GeneratedName? = null,
    val nameCharCount: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    fun updateFromGeneratedName(generatedName: GeneratedName) {
        ProfileUpdater.updateFromGeneratedName(this, generatedName)
    }

    fun getFullName(): String {
        return ProfileStringFormatter.getFullName(this)
    }

    fun getFullNameWithHanja(): String {
        return ProfileStringFormatter.getFullNameWithHanja(this)
    }

    fun getBirthDateString(): String {
        return ProfileStringFormatter.getBirthDateString(this)
    }

    fun getSimpleBirthDate(): String {
        return ProfileStringFormatter.getSimpleBirthDate(this)
    }

    fun getBirthTimeString(): String {
        return ProfileStringFormatter.getBirthTimeString(this)
    }

    fun getScoreThemeColor(): ScoreTheme {
        return ProfileEvaluationStatus.getScoreThemeColor(this)
    }

    fun isEvaluated(): Boolean {
        return ProfileEvaluationStatus.isEvaluated(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Profile) return false

        return profileName == other.profileName &&
                birthDate.timeInMillis == other.birthDate.timeInMillis &&
                surname?.korean == other.surname?.korean &&
                surname?.hanja == other.surname?.hanja
    }

    override fun hashCode(): Int {
        var result = profileName.hashCode()
        result = 31 * result + birthDate.timeInMillis.hashCode()
        result = 31 * result + (surname?.korean?.hashCode() ?: 0)
        result = 31 * result + (surname?.hanja?.hashCode() ?: 0)
        return result
    }

    enum class ScoreTheme {
        SUNNY_SPRING,
        WARM_SPRING,
        CLOUDY_SPRING,
        RAINY_SPRING,
        COLD_SPRING,
        NOT_EVALUATED
    }
}