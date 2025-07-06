// model/domain/usecase/ProfileManagerProvider.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager
import com.ssc.namespring.model.domain.service.profile.ProfileManagerImpl

/**
 * ProfileManager의 기본 구현체를 제공하는 Provider
 * 앱 전체에서 사용할 ProfileManager 인스턴스를 관리
 */
object ProfileManagerProvider {
    private const val TAG = "ProfileManagerProvider"

    @Volatile
    private var instance: ProfileManager? = null

    /**
     * ProfileManager 인스턴스 초기화
     */
    @JvmStatic
    fun init(context: Context) {
        if (instance != null) {
            Log.d(TAG, "ProfileManager already initialized")
            return
        }

        synchronized(this) {
            if (instance == null) {
                val container = ProfileDependencyContainer(context.applicationContext)
                val implementation = ProfileManagerImpl(container)
                implementation.init()
                instance = ProfileManagerAdapter(implementation)
                Log.d(TAG, "ProfileManager initialized successfully")
            }
        }
    }

    /**
     * ProfileManager 인스턴스 가져오기
     */
    @JvmStatic
    fun getInstance(): ProfileManager {
        return instance ?: throw IllegalStateException(
            "ProfileManager is not initialized. Call ProfileManagerProvider.init(context) first."
        )
    }

    /**
     * 테스트용 인스턴스 설정
     */
    @JvmStatic
    internal fun setInstance(profileManager: ProfileManager) {
        synchronized(this) {
            instance = profileManager
        }
    }

    /**
     * 인스턴스 리셋 (테스트용)
     */
    @JvmStatic
    internal fun reset() {
        synchronized(this) {
            instance = null
        }
    }
}

/**
 * IProfileManager를 ProfileManager 인터페이스로 변환하는 어댑터
 */
private class ProfileManagerAdapter(
    private val implementation: IProfileManager
) : ProfileManager {

    override fun addProfile(profile: Profile): Boolean =
        implementation.addProfile(profile)

    override fun updateProfile(profile: Profile): Boolean =
        implementation.updateProfile(profile)

    override fun deleteProfiles(profileIds: List<String>) =
        implementation.deleteProfiles(profileIds)

    override fun deleteProfile(profileId: String) =
        deleteProfiles(listOf(profileId))

    override fun isDuplicateProfile(profile: Profile): Boolean =
        implementation.isDuplicateProfile(profile)

    override fun searchProfiles(query: String): List<Profile> =
        implementation.searchProfiles(query)

    override fun getSortedProfiles(sortType: ProfileManager.SortType): List<Profile> {
        val implSortType = when (sortType) {
            ProfileManager.SortType.NAME_ASC -> IProfileManager.SortType.NAME_ASC
            ProfileManager.SortType.NAME_DESC -> IProfileManager.SortType.NAME_DESC
            ProfileManager.SortType.SCORE_DESC -> IProfileManager.SortType.SCORE_DESC
            ProfileManager.SortType.SCORE_ASC -> IProfileManager.SortType.SCORE_ASC
            ProfileManager.SortType.DATE_DESC -> IProfileManager.SortType.DATE_DESC
            ProfileManager.SortType.DATE_ASC -> IProfileManager.SortType.DATE_ASC
        }
        return implementation.getSortedProfiles(implSortType)
    }

    override fun getAllProfiles(): List<Profile> =
        implementation.getAllProfiles()

    override fun getProfile(id: String): Profile? =
        implementation.getProfile(id)

    override fun getCurrentProfile(): Profile? =
        implementation.getCurrentProfile()

    override fun hasProfiles(): Boolean =
        implementation.hasProfiles()

    override fun setSelectedProfile(profile: Profile) =
        implementation.setSelectedProfile(profile)

    override fun getSelectedProfile(): Profile? =
        implementation.getSelectedProfile()

    override fun switchProfile(id: String) =
        implementation.switchProfile(id)
}