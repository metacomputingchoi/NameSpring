// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.utils.JsonFileLoader
import com.ssc.namespring.utils.NameGeneratorTester
import com.ssc.namespring.utils.NamingSystemBuilder
import com.ssc.namespring.utils.logger.AndroidLogger
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var jsonFileLoader: JsonFileLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        initializeComponents()
        startInitialization()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initializeComponents() {
        jsonFileLoader = JsonFileLoader(assets)
    }

    private fun startInitialization() {
        showLoading(true)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Builder 패턴으로 NamingSystem 생성 및 초기화
                val namingSystem = NamingSystemBuilder()
                    .withLogger(AndroidLogger("NamingSystem"))
                    .withJsonFileLoader(jsonFileLoader)
                    .buildAndInitialize()

                // UI 업데이트는 메인 스레드에서
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "NamingSystem 초기화 완료")
                    showLoading(false)
                }

                // 테스트 실행
                val nameGeneratorTester = NameGeneratorTester(namingSystem)
                nameGeneratorTester.runAllTests()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "초기화 중 오류 발생", e)
                    showError("초기화 중 오류가 발생했습니다: ${e.message}")
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            Log.d(TAG, "로딩 시작...")
        } else {
            Log.d(TAG, "로딩 완료")
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, message)
    }
}