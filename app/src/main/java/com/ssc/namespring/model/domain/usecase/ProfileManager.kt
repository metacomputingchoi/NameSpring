// model/domain/usecase/ProfileManager.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.interfaces.IProfileManager

/**
 * ProfileManager - 앱 전역에서 프로필 관리 기능을 제공하는 싱글톤
 *
 * 리팩토링 포인트:
 * - 안전한 초기화 패턴 적용 (lazy initialization)
 * - 의존성 주입을 통한 테스트 가능성 확보
 * - Thread-safe 보장
 */
object ProfileManager {
    private const val TAG = "ProfileManager"

    @Volatile
    private var instance: ProfileManagerInstance? = null

    // 기존 SortType 유지 (호환성)
    enum class SortType {
        NAME_ASC, NAME_DESC, SCORE_DESC, SCORE_ASC, DATE_DESC, DATE_ASC
    }

    /**
     * ProfileManager 초기화
     * Thread-safe double-checked locking 패턴 사용
     */
    @JvmStatic
    fun init(context: Context) {
        if (instance != null) {
            Log.d(TAG, "ProfileManager already initialized")
            return
        }

        synchronized(this) {
            if (instance == null) {
                instance = ProfileManagerInstance(context.applicationContext)
                Log.d(TAG, "ProfileManager initialized successfully")
            }
        }
    }

    /**
     * 테스트용 초기화 메서드
     * 의존성을 직접 주입할 수 있도록 함
     */
    @JvmStatic
    internal fun initForTest(implementation: IProfileManager) {
        synchronized(this) {
            instance = ProfileManagerInstance(implementation)
            Log.d(TAG, "ProfileManager initialized for testing")
        }
    }

    /**
     * ProfileManager 인스턴스 리셋 (테스트용)
     */
    @JvmStatic
    internal fun reset() {
        synchronized(this) {
            instance = null
            Log.d(TAG, "ProfileManager reset")
        }
    }

    private fun requireInstance(): ProfileManagerInstance {
        return instance ?: throw IllegalStateException(
            "ProfileManager is not initialized. Call ProfileManager.init(context) first."
        )
    }

    // Public API methods
    fun addProfile(profile: Profile): Boolean =
        requireInstance().addProfile(profile)

    fun updateProfile(profile: Profile): Boolean =
        requireInstance().updateProfile(profile)

    fun deleteProfiles(profileIds: List<String>) =
        requireInstance().deleteProfiles(profileIds)

    fun deleteProfile(profileId: String) =
        deleteProfiles(listOf(profileId))

    fun isDuplicateProfile(profile: Profile): Boolean =
        requireInstance().isDuplicateProfile(profile)

    fun searchProfiles(query: String): List<Profile> =
        requireInstance().searchProfiles(query)

    fun getSortedProfiles(sortType: SortType): List<Profile> =
        requireInstance().getSortedProfiles(sortType)

    fun getAllProfiles(): List<Profile> =
        requireInstance().getAllProfiles()

    fun getProfile(id: String): Profile? =
        requireInstance().getProfile(id)

    fun getCurrentProfile(): Profile? =
        requireInstance().getCurrentProfile()

    fun hasProfiles(): Boolean =
        requireInstance().hasProfiles()

    fun setSelectedProfile(profile: Profile) =
        requireInstance().setSelectedProfile(profile)

    fun getSelectedProfile(): Profile? =
        requireInstance().getSelectedProfile()

    fun switchProfile(id: String) =
        requireInstance().switchProfile(id)
}