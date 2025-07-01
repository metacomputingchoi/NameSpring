// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.NameGeneratorTester
import com.ssc.namespring.utils.logger.AndroidLogger
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var namingEngine: NamingEngine
    private lateinit var nameGeneratorTester: NameGeneratorTester

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        initializeComponents()
        startInitialization()
    }

    private fun initializeComponents() {
        try {
            namingEngine = NamingEngine.create(
                logger = AndroidLogger("NamingEngine")
            )

            nameGeneratorTester = NameGeneratorTester(namingEngine)

            Log.d(TAG, "모든 컴포넌트 초기화 완료")
        } catch (e: Exception) {
            Log.e(TAG, "컴포넌트 초기화 실패", e)
        }
    }

    private fun startInitialization() {
        showLoading(true)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                Log.d(TAG, "NamingEngine 초기화 완료")
                Log.d(TAG, "NamingEngine 버전: ${NamingEngine.VERSION}")

                showLoading(false)

                // 테스트 실행
                nameGeneratorTester.runAllTests()

            } catch (e: Exception) {
                Log.e(TAG, "초기화 중 오류 발생", e)
                withContext(Dispatchers.Main) {
                    showError("초기화 중 오류가 발생했습니다: ${e.message}")
                }
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        runOnUiThread {
            if (show) {
                Log.d(TAG, "로딩 시작...")
            } else {
                Log.d(TAG, "로딩 완료")
            }
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, message)
    }
}