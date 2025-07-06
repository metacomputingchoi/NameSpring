// model/data/source/DataLoader.kt
package com.ssc.namespring.model.data.source

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.entity.SurnameData
import com.ssc.namespring.model.domain.entity.ValidationResult
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

object DataLoader {
    private const val TAG = "DataLoader"

    private val isInitialized = AtomicBoolean(false)
    private val isInitializing = AtomicBoolean(false)
    private var initJob: Job? = null

    private val nameDataService: INameDataService by lazy {
        NameDataServiceFactory.getInstance()
    }

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
                    withContext(Dispatchers.Main) {
                        listener?.onProgress(0, "데이터 로딩 시작...")
                    }

                    withContext(Dispatchers.Main) {
                        listener?.onProgress(25, "이름 데이터 로딩 중...")
                    }

                    try {
                        nameDataService.init(context)
                        Log.d(TAG, "NameData 초기화 성공")
                    } catch (e: Exception) {
                        Log.e(TAG, "NameData 초기화 실패", e)
                        throw Exception("이름 데이터 초기화 실패: ${e.message}")
                    }

                    withContext(Dispatchers.Main) {
                        listener?.onProgress(50, "성씨 데이터 로딩 중...")
                    }

                    try {
                        SurnameData.init(context)
                        Log.d(TAG, "SurnameData 초기화 성공")
                    } catch (e: Exception) {
                        Log.e(TAG, "SurnameData 초기화 실패", e)
                        Log.w(TAG, "성씨 데이터 로드 실패했지만 계속 진행")
                    }

                    withContext(Dispatchers.Main) {
                        listener?.onProgress(75, "데이터 검증 중...")
                    }

                    val validationResult = validateAllData()

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

    private fun validateAllData(): ValidationResult {
        val warnings = mutableListOf<String>()
        val criticalErrors = mutableListOf<String>()

        val nameValidation = nameDataService.validateData()
        warnings.addAll(nameValidation.warnings)
        criticalErrors.addAll(nameValidation.criticalErrors)

        val surnameValidation = SurnameData.validateData()
        warnings.addAll(surnameValidation.warnings)

        return ValidationResult(
            isValid = criticalErrors.isEmpty(),
            warnings = warnings,
            criticalErrors = criticalErrors
        )
    }

    fun isReady(): Boolean = isInitialized.get()
}