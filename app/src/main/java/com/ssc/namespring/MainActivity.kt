// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.utils.JsonFileLoader
import com.ssc.namespring.utils.NameGeneratorTester
import com.ssc.namespring.utils.NamingSystemInitializer
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var jsonFileLoader: JsonFileLoader
    private lateinit var namingSystemInitializer: NamingSystemInitializer
    private lateinit var nameGeneratorTester: NameGeneratorTester

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        initializeComponents()
        startInitialization()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    private fun initializeComponents() {
        jsonFileLoader = JsonFileLoader(assets)
        namingSystemInitializer = NamingSystemInitializer(jsonFileLoader)
        nameGeneratorTester = NameGeneratorTester()
    }

    private fun startInitialization() {
        mainScope.launch {
            try {
                showLoading(true)

                namingSystemInitializer.initialize()

                Log.d(TAG, "NamingSystem 초기화 완료")
                showLoading(false)

                nameGeneratorTester.runAllTests()

            } catch (e: Exception) {
                Log.e(TAG, "초기화 중 오류 발생", e)
                showError("초기화 중 오류가 발생했습니다: ${e.message}")
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