// model/domain/usecase/profile/ProfileManagerSingleton.kt
package com.ssc.namespring.model.domain.usecase.profile

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.usecase.ProfileManager

/**
 * ProfileManager 싱글톤 인스턴스 관리
 */
internal object ProfileManagerSingleton {
    private const val TAG = "ProfileManagerSingleton"

    @Volatile
    private var instance: ProfileManager? = null

    fun init(context: Context, factory: (Context) -> ProfileManager) {
        if (instance != null) {
            Log.d(TAG, "ProfileManager already initialized")
            return
        }

        synchronized(this) {
            if (instance == null) {
                instance = factory(context.applicationContext)
                Log.d(TAG, "ProfileManager initialized successfully")
            }
        }
    }

    fun getInstance(): ProfileManager {
        return instance ?: throw IllegalStateException(
            "ProfileManager is not initialized. Call ProfileManagerProvider.init(context) first."
        )
    }

    fun setInstance(profileManager: ProfileManager) {
        synchronized(this) {
            instance = profileManager
        }
    }

    fun reset() {
        synchronized(this) {
            instance = null
        }
    }
}