// model/domain/usecase/SplashManager.kt
package com.ssc.namespring.model.domain.usecase

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.utils.data.json.JsonLoader
import kotlinx.coroutines.delay

class SplashManager {
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Loading(0, ""))
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val minSplashTime = 1500L

    fun loadData(context: Context) {
        scope.launch {
            val startTime = System.currentTimeMillis()

            try {
                ProfileManagerProvider.init(context)
                // JsonLoader 초기화 추가
                JsonLoader.initialize(context)

                DataLoader.ensureInitialized(context, object : DataLoader.LoadingListener {

                    override fun onProgress(progress: Int, message: String) {
                        _loadingState.value = LoadingState.Loading(progress, message)
                    }

                    override fun onComplete() {
                        val elapsedTime = System.currentTimeMillis() - startTime
                        val remainingTime = minSplashTime - elapsedTime

                        if (remainingTime > 0) {
                            scope.launch {
                                delay(remainingTime)
                                _loadingState.value = LoadingState.Success
                            }
                        } else {
                            _loadingState.value = LoadingState.Success
                        }
                    }

                    override fun onError(error: String) {
                        _loadingState.value = LoadingState.Error("$error\n\n앱을 다시 시작해주세요.")
                    }
                })
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("초기화 오류: ${e.message}")
            }
        }
    }

    sealed class LoadingState {
        data class Loading(val progress: Int, val message: String) : LoadingState()
        object Success : LoadingState()
        data class Error(val message: String) : LoadingState()
    }
}