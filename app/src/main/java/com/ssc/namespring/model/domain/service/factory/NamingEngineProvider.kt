// model/domain/service/factory/NamingEngineProvider.kt
package com.ssc.namespring.model.domain.service.factory

import android.util.Log
import com.ssc.namingengine.NamingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * NamingEngine의 싱글톤 프로바이더
 * 앱 전체에서 하나의 인스턴스만 사용하도록 보장
 */
object NamingEngineProvider {
    private const val TAG = "NamingEngineProvider"

    @Volatile
    private var instance: NamingEngine? = null

    /**
     * NamingEngine 인스턴스를 반환
     * 초기화되지 않았으면 자동으로 초기화
     */
    fun getInstance(): NamingEngine {
        return instance ?: synchronized(this) {
            instance ?: createInstance().also { instance = it }
        }
    }

    /**
     * 비동기로 미리 초기화
     * SplashActivity에서 호출
     */
    suspend fun preInitialize() = withContext(Dispatchers.IO) {
        if (instance == null) {
            Log.d(TAG, "Pre-initializing NamingEngine...")
            val startTime = System.currentTimeMillis()

            try {
                getInstance()
                val elapsedTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "NamingEngine initialized successfully in ${elapsedTime}ms")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize NamingEngine", e)
                throw e
            }
        } else {
            Log.d(TAG, "NamingEngine already initialized")
        }
    }

    /**
     * NamingEngine 인스턴스 생성
     */
    private fun createInstance(): NamingEngine {
        Log.d(TAG, "Creating new NamingEngine instance...")
        return NamingEngine.create()
    }

    /**
     * 초기화 여부 확인
     */
    fun isInitialized(): Boolean = instance != null
}