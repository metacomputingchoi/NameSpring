// model/domain/usecase/ProfileManagerProvider.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.service.profile.ProfileManagerImpl
import com.ssc.namespring.model.domain.usecase.profile.ProfileManagerAdapter
import com.ssc.namespring.model.domain.usecase.profile.ProfileManagerSingleton

/**
 * ProfileManager의 기본 구현체를 제공하는 Provider
 * 앱 전체에서 사용할 ProfileManager 인스턴스를 관리
 */
object ProfileManagerProvider {
    private const val TAG = "ProfileManagerProvider"

    /**
     * ProfileManager 인스턴스 초기화
     */
    @JvmStatic
    fun init(context: Context) {
        ProfileManagerSingleton.init(context) { appContext ->
            val container = ProfileDependencyContainer(appContext)
            val implementation = ProfileManagerImpl(container)
            implementation.init()
            ProfileManagerAdapter(implementation)
        }
    }

    /**
     * ProfileManager 인스턴스 가져오기
     */
    @JvmStatic
    fun getInstance(): ProfileManager = ProfileManagerSingleton.getInstance()

    /**
     * 테스트용 인스턴스 설정
     */
    @JvmStatic
    internal fun setInstance(profileManager: ProfileManager) {
        ProfileManagerSingleton.setInstance(profileManager)
    }

    /**
     * 인스턴스 리셋 (테스트용)
     */
    @JvmStatic
    internal fun reset() {
        ProfileManagerSingleton.reset()
    }
}