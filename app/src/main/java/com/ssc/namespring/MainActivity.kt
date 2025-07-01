// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namingengine.NamingEngineSDK
import com.ssc.namingengine.api.NamingEngineAPI
import com.ssc.namingengine.core.NamingSystem
import com.ssc.namespring.model.NameGeneratorTester
import com.ssc.namespring.utils.logger.AndroidLogger
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var namingSystem: NamingSystem
    private lateinit var namingEngineAPI: NamingEngineAPI
    private lateinit var nameGeneratorTester: NameGeneratorTester

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        initializeComponents()
        startInitialization()
    }

    private fun initializeComponents() {
        try {
            namingSystem = NamingEngineSDK.create(
                context = this,
                logger = AndroidLogger("NamingEngine")
            )

            namingEngineAPI = NamingEngineAPI(namingSystem)
            nameGeneratorTester = NameGeneratorTester(namingEngineAPI)

            Log.d(TAG, "모든 컴포넌트 초기화 완료")
        } catch (e: Exception) {
            Log.e(TAG, "컴포넌트 초기화 실패", e)
        }
    }

    private fun startInitialization() {
        showLoading(true)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "NamingEngine SDK 초기화 완료")
                    showLoading(false)
                }

                val apiInfo = namingEngineAPI.getApiInfo()
                Log.d(TAG, "API 버전: ${apiInfo.version}")
                Log.d(TAG, "API 기능: ${apiInfo.capabilities.joinToString(", ")}")

                nameGeneratorTester.runAllTests()
                runCustomTest()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "초기화 중 오류 발생", e)
                    showError("초기화 중 오류가 발생했습니다: ${e.message}")
                    showLoading(false)
                }
            }
        }
    }

    private fun runCustomTest() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val result = nameGeneratorTester.runCustomTest(
                    userInput = "[박/朴][_/_][_/_]",
                    birthDateTime = java.time.LocalDateTime.now(),
                    withoutFilter = false,
                    description = "박씨 성을 가진 이름 생성 테스트"
                )

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "커스텀 테스트 완료: ${result.results.size}개 생성됨")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "커스텀 테스트 실행 중 오류", e)
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