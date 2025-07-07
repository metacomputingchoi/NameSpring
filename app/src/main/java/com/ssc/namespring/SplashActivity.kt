// SplashActivity.kt
package com.ssc.namespring

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.domain.service.factory.NamingEngineProvider
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.data.source.DataLoader
import com.ssc.namespring.utils.data.json.JsonLoader
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SplashActivity"
        private const val MIN_SPLASH_TIME = 1500L
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvStatus: TextView

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_layout)

        initViews()
        loadAllData()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun loadAllData() {
        scope.launch {
            val startTime = System.currentTimeMillis()

            try {
                // 1단계: ProfileManager 초기화 (10%)
                updateProgress(10, "프로필 매니저 초기화 중...")
                withContext(Dispatchers.IO) {
                    ProfileManagerProvider.init(this@SplashActivity)
                }
                Log.d(TAG, "ProfileManager initialized successfully")

                // 2단계: TaskWorkManager 초기화 (15%)
                updateProgress(15, "작업 매니저 초기화 중...")
                withContext(Dispatchers.IO) {
                    TaskWorkManager.getInstance(this@SplashActivity)
                }
                Log.d(TAG, "TaskWorkManager initialized successfully")

                // 3단계: JsonLoader 초기화 (20%)
                updateProgress(20, "JSON 데이터 로딩 중...")
                withContext(Dispatchers.IO) {
                    JsonLoader.initialize(this@SplashActivity)
                }
                Log.d(TAG, "JsonLoader initialized successfully")

                // 4단계: NamingEngine 초기화 (30-40%)
                updateProgress(30, "작명 엔진 초기화 중...")
                withContext(Dispatchers.IO) {
                    NamingEngineProvider.preInitialize()
                }
                updateProgress(40, "작명 엔진 초기화 완료")
                Log.d(TAG, "NamingEngine initialized successfully")

                // 5단계: 나머지 데이터 로드 (40-90%)
                updateProgress(50, "이름 데이터 로딩 중...")

                val loadingComplete = CompletableDeferred<Boolean>()

                DataLoader.ensureInitialized(this@SplashActivity, object : DataLoader.LoadingListener {
                    override fun onProgress(progress: Int, message: String) {
                        // DataLoader의 progress를 50-90 범위로 매핑
                        val mappedProgress = 50 + (progress * 40 / 100)
                        updateProgress(mappedProgress, message)
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

                // DataLoader 완료 대기
                loadingComplete.await()

                // 6단계: 최종 검증 (90-100%)
                updateProgress(95, "초기화 완료 중...")

                // 최소 스플래시 시간 보장
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = MIN_SPLASH_TIME - elapsedTime
                if (remainingTime > 0) {
                    delay(remainingTime)
                }

                updateProgress(100, "완료!")
                delay(100) // 100% 표시를 잠깐 보여주기

                // 메인 화면으로 이동
                navigateToMain()

            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                showError("초기화 실패: ${e.message}\n\n앱을 다시 시작해주세요.")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int, message: String) {
        runOnUiThread {
            progressBar.progress = progress
            tvProgress.text = "$progress%"
            tvStatus.text = message
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, ProfileListActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("초기화 실패")
                .setMessage(message)
                .setPositiveButton("종료") { _, _ ->
                    finishAffinity() // 앱 완전 종료
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}