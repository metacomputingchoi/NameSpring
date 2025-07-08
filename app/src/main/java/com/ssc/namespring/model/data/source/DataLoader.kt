// model/data/source/DataLoader.kt
package com.ssc.namespring.model.data.source

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.data.source.interfaces.LoadingListener
import com.ssc.namespring.model.data.source.initializers.DataInitializer
import com.ssc.namespring.model.data.source.validators.DataValidator
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

object DataLoader {
    private const val TAG = "DataLoader"

    private val isInitialized = AtomicBoolean(false)
    private val isInitializing = AtomicBoolean(false)
    private var initJob: Job? = null

    private val nameDataService by lazy { NameDataServiceFactory.getInstance() }
    private val dataInitializer by lazy { DataInitializer(nameDataService) }
    private val dataValidator by lazy { DataValidator(nameDataService) }

    interface LoadingListener {
        fun onProgress(progress: Int, message: String)
        fun onComplete()
        fun onError(error: String)
    }

    suspend fun ensureInitialized(context: Context, listener: LoadingListener? = null) {
        if (isInitialized.get()) {
            listener?.onComplete()
            return
        }

        if (isInitializing.compareAndSet(false, true)) {
            initJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    dataInitializer.initializeData(context) { progress, message ->
                        listener?.onProgress(progress, message)
                    }

                    val validationResult = dataValidator.validateAllData()

                    if (validationResult.criticalErrors.isNotEmpty()) {
                        throw Exception("치명적 오류: ${validationResult.criticalErrors.joinToString(", ")}")
                    }

                    isInitialized.set(true)

                    withContext(Dispatchers.Main) {
                        listener?.onProgress(100, "완료!")
                        listener?.onComplete()
                    }

                    Log.d(TAG, "모든 데이터 로드 완료")

                } catch (e: Exception) {
                    Log.e(TAG, "데이터 로드 실패", e)
                    withContext(Dispatchers.Main) {
                        listener?.onError(e.message ?: "알 수 없는 오류")
                    }
                    isInitialized.set(false)
                } finally {
                    isInitializing.set(false)
                }
            }
        } else {
            initJob?.join()
            if (isInitialized.get()) {
                listener?.onComplete()
            } else {
                listener?.onError("데이터 초기화 실패")
            }
        }
    }

    fun isReady(): Boolean = isInitialized.get()
}