// model/domain/usecase/ProfileDependencyContainer.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.data.mapper.ProfileMigrator
import com.ssc.namespring.model.data.repository.ProfileRepository
import com.ssc.namespring.model.domain.service.interfaces.*
import com.ssc.namespring.model.domain.service.profile.ProfileEvaluationService
import com.ssc.namespring.model.domain.service.profile.ProfileServiceImpl

/**
 * 의존성 주입을 위한 컨테이너
 * 향후 Hilt나 Koin으로 교체 가능
 */
internal class ProfileDependencyContainer(private val context: Context) {

    companion object {
        private const val TAG = "ProfileDependencyContainer"
        private const val PREF_NAME = "namespring_profiles"
    }

    private val gson: Gson by lazy { Gson() }

    private val namingEngine: NamingEngine by lazy {
        try {
            NamingEngine.create().also {
                Log.d(TAG, "NamingEngine initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize NamingEngine", e)
            throw RuntimeException("NamingEngine initialization failed", e)
        }
    }

    private val repository: IProfileRepository by lazy {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        ProfileRepositoryAdapter(ProfileRepository(sharedPreferences, gson))
    }

    private val service: IProfileService by lazy {
        ProfileServiceAdapter(ProfileServiceImpl())
    }

    private val evaluator: IProfileEvaluator by lazy {
        ProfileEvaluatorAdapter(ProfileEvaluationService(namingEngine))
    }

    private val migrator: IProfileMigrator by lazy {
        ProfileMigratorAdapter(ProfileMigrator(gson))
    }

    fun provideProfileUseCase(): ProfileUseCase {
        return ProfileUseCase(repository, service, evaluator, migrator)
    }
}