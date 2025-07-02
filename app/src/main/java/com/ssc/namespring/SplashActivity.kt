// SplashActivity.kt
package com.ssc.namespring

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.DataLoader
import com.ssc.namespring.model.NameData
import com.ssc.namespring.model.ProfileManager
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

        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        tvStatus = findViewById(R.id.tvStatus)

        // 프로그레스 UI 초기 설정
        progressBar.visibility = View.VISIBLE
        tvProgress.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE

        loadData()
    }

    private fun loadData() {
        scope.launch {
            val startTime = System.currentTimeMillis()

            try {
                // ProfileManager 초기화
                ProfileManager.init(this@SplashActivity)

                // DataLoader를 통한 데이터 로드
                DataLoader.ensureInitialized(this@SplashActivity, object : DataLoader.LoadingListener {
                    override fun onProgress(progress: Int, message: String) {
                        runOnUiThread {
                            progressBar.progress = progress
                            tvProgress.text = "$progress%"
                            tvStatus.text = message
                        }
                    }

                    override fun onComplete() {
                        runOnUiThread {
                            // NameData가 제대로 초기화되었는지 확인
                            if (!NameData.isReady()) {
                                showInitializationError("이름 데이터 초기화 실패")
                                return@runOnUiThread
                            }

                            progressBar.progress = 100
                            tvProgress.text = "100%"
                            tvStatus.text = "완료!"

                            val elapsedTime = System.currentTimeMillis() - startTime
                            val remainingTime = MIN_SPLASH_TIME - elapsedTime

                            if (remainingTime > 0) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    navigateToNextScreen()
                                }, remainingTime)
                            } else {
                                navigateToNextScreen()
                            }
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            showInitializationError(error)
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Initialization error", e)
                runOnUiThread {
                    showInitializationError("초기화 오류: ${e.message}")
                }
            }
        }
    }

    private fun showInitializationError(error: String) {
        AlertDialog.Builder(this)
            .setTitle("초기화 실패")
            .setMessage("$error\n\n앱을 다시 시작해주세요.")
            .setPositiveButton("종료") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToNextScreen() {
        val nextIntent = Intent(this, ProfileListActivity::class.java)
        startActivity(nextIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}