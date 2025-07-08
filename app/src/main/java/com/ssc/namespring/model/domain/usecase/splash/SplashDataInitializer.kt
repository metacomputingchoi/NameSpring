// model/domain/usecase/splash/SplashDataInitializer.kt
package com.ssc.namespring.model.domain.usecase.splash

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.model.domain.service.factory.NamingEngineProvider
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.utils.data.json.JsonLoader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SplashDataInitializer(
    private val context: Context,
    private val progressCallback: (Int, String) -> Unit
) {
    companion object {
        private const val TAG = "SplashDataInitializer"
    }

    suspend fun initializeAll() {
        initializeProfileManager()
        initializeTaskWorkManager()
        initializeJsonLoader()
        initializeNamingEngine()
        initializeDataLoader()
        finalizeInitialization()
    }

    private suspend fun initializeProfileManager() {
        progressCallback(SplashInitializationSteps.PROFILE_MANAGER_PROGRESS, "프로필 매니저 초기화 중...")
        withContext(Dispatchers.IO) {
            ProfileManagerProvider.init(context)
        }
        Log.d(TAG, "ProfileManager initialized successfully")
    }

    private suspend fun initializeTaskWorkManager() {
        progressCallback(SplashInitializationSteps.TASK_MANAGER_PROGRESS, "작업 매니저 초기화 중...")
        withContext(Dispatchers.IO) {
            TaskWorkManager.getInstance(context)
        }
        Log.d(TAG, "TaskWorkManager initialized successfully")
    }

    private suspend fun initializeJsonLoader() {
        progressCallback(SplashInitializationSteps.JSON_LOADER_PROGRESS, "JSON 데이터 로딩 중...")
        withContext(Dispatchers.IO) {
            JsonLoader.initialize(context)
        }
        Log.d(TAG, "JsonLoader initialized successfully")
    }

    private suspend fun initializeNamingEngine() {
        progressCallback(SplashInitializationSteps.NAMING_ENGINE_START_PROGRESS, "작명 엔진 초기화 중...")
        withContext(Dispatchers.IO) {
            NamingEngineProvider.preInitialize()
        }
        progressCallback(SplashInitializationSteps.NAMING_ENGINE_END_PROGRESS, "작명 엔진 초기화 완료")
        Log.d(TAG, "NamingEngine initialized successfully")
    }

    private suspend fun initializeDataLoader() {
        progressCallback(SplashInitializationSteps.DATA_LOADER_START_PROGRESS, "이름 데이터 로딩 중...")

        val loadingComplete = CompletableDeferred<Boolean>()

        DataLoader.ensureInitialized(context, object : DataLoader.LoadingListener {
            override fun onProgress(progress: Int, message: String) {
                val mappedProgress = SplashInitializationSteps.DATA_LOADER_START_PROGRESS + 
                    (progress * (SplashInitializationSteps.DATA_LOADER_END_PROGRESS - 
                    SplashInitializationSteps.DATA_LOADER_START_PROGRESS) / 100)
                progressCallback(mappedProgress, message)
            }

            override fun onComplete() {
                Log.d(TAG, "DataLoader initialization complete")
                loadingComplete.complete(true)
            }

            override fun onError(error: String) {
                Log.e(TAG, "DataLoader initialization failed: $error")
                loadingComplete.completeExceptionally(Exception(error))
            }
        })

        loadingComplete.await()
    }

    private fun finalizeInitialization() {
        progressCallback(SplashInitializationSteps.FINAL_VERIFICATION_PROGRESS, "초기화 완료 중...")
    }
}