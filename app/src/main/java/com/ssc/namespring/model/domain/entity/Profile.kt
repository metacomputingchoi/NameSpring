// model/domain/entity/Profile.kt
package com.ssc.namespring.model.domain.entity

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.Expose
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
    var nameBomScore: Int = 0,  // var여야 함
    var sajuInfo: SajuInfo? = null,  // var여야 함
    var ohaengInfo: OhaengInfo? = null,  // var여야 함
    var evaluatedNameJson: String? = null,  // var여야 함
    val nameCharCount: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    companion object {
        private const val TAG = "Profile"
        private val gson = Gson()
    }

    // evaluatedName은 필요할 때만 JSON에서 파싱
    val evaluatedName: GeneratedName?
        get() = evaluatedNameJson?.let {
            try {
                gson.fromJson(it, GeneratedName::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse evaluatedNameJson", e)
                null
            }
        }

    fun updateEvaluatedName(generatedName: GeneratedName?) {
        this.evaluatedNameJson = generatedName?.let {
            try {
                gson.toJson(it)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to serialize GeneratedName", e)
                null
            }
        }
        Log.d(TAG, "updateEvaluatedName: json length = ${evaluatedNameJson?.length}")
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