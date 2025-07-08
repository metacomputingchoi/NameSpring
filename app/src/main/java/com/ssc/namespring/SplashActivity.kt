// SplashActivity.kt
package com.ssc.namespring

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.domain.usecase.splash.SplashDataInitializer
import com.ssc.namespring.model.domain.usecase.splash.SplashInitializationSteps
import com.ssc.namespring.ui.splash.SplashProgressManager
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SplashActivity"
        private const val MIN_SPLASH_TIME = 1500L
    }

    private lateinit var progressManager: SplashProgressManager
    private lateinit var dataInitializer: SplashDataInitializer
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_layout)
        ProfileManagerProvider.init(this)
        initComponents()
        startInitialization()
    }

    private fun initComponents() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        progressManager = SplashProgressManager(progressBar, tvProgress, tvStatus)
        dataInitializer = SplashDataInitializer(this) { progress, message ->
            runOnUiThread { progressManager.updateProgress(progress, message) }
        }
    }

    private fun startInitialization() {
        scope.launch {
            val startTime = System.currentTimeMillis()

            try {
                dataInitializer.initializeAll()

                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = MIN_SPLASH_TIME - elapsedTime
                if (remainingTime > 0) {
                    delay(remainingTime)
                }

                runOnUiThread { 
                    progressManager.updateProgress(
                        SplashInitializationSteps.COMPLETE_PROGRESS, 
                        "완료!"
                    ) 
                }
                delay(100)

                navigateToMain()

            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                showError("초기화 실패: ${e.message}\n\n앱을 다시 시작해주세요.")
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, ProfileListActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("초기화 실패")
                .setMessage(message)
                .setPositiveButton("종료") { _, _ ->
                    finishAffinity()
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