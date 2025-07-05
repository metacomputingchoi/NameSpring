// SplashActivity.kt
package com.ssc.namespring

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.usecase.SplashManager
import com.ssc.namespring.model.domain.usecase.SplashManager.LoadingState
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvStatus: TextView

    private val viewModel = SplashManager()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_layout)

        initViews()
        observeViewModel()
        viewModel.loadData(this)
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun observeViewModel() {
        scope.launch {
            viewModel.loadingState.collect { state ->
                when (state) {
                    is LoadingState.Loading -> updateProgress(state.progress, state.message)
                    is LoadingState.Success -> navigateToMain()
                    is LoadingState.Error -> showError(state.message)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int, message: String) {
        progressBar.progress = progress
        tvProgress.text = "$progress%"
        tvStatus.text = message
    }

    private fun navigateToMain() {
        startActivity(Intent(this, ProfileListActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("초기화 실패")
            .setMessage(message)
            .setPositiveButton("종료") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
