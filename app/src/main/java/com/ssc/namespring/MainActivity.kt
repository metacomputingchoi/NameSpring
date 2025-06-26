// MainActivity.kt
package com.ssc.namespring

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.utils.JsonFileLoader
import com.ssc.namespring.utils.NameGeneratorTester
import com.ssc.namespring.utils.NamingSystemInitializer
// import com.google.android.material.color.MaterialColors: hierarchy 구조가 패키지의 의도다.
// import com.samsung.~~
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var jsonFileLoader: JsonFileLoader
    private lateinit var namingSystemInitializer: NamingSystemInitializer
    private lateinit var nameGeneratorTester: NameGeneratorTester
    private var btn: Button? = null
    private var activity: Activity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        // btn.visibility = View.VISIBLE
        // Constants였다면, 상수를 만든 개발자의 의도에 맞지 않는 형태로 사용하려고 시도하는 다른 개발자가 나타나버림.
        // 잘하는 개발자들은 거기서 감탄.. 와 대박 점마 개 세심하다. 배려 쩐다. 개잘하네 (모르는 다른사람이 봐도 알게끔 만들어버리는 클라스)
        // 디자인패턴이 그래서 있는거임. 딱봐도 뭐하는놈인지 1초만에 알아채게 만드는놈
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
        showLoading(true)
        CoroutineScope(Dispatchers.Default).launch { // 쓰레드라 상상해보자
            // 쓰레드의 작업 시작
            // show loading true (main thread에게 메시징)
            // 여기서 하는 작업은 코루틴이 아니라 쓰레드였더라도 메인쓰레드는 블로킹 안하겠죠 당연히.
            // suspend 가 아닌 function 이어도 당연히 가능하단 말임
            // 쓰레드의 작업 종료
            // show loading false (main thread에게 메시징)
        }

        mainScope.launch {
            try {
                namingSystemInitializer.initialize()

                Log.d(TAG, "NamingSystem 초기화 완료")
                showLoading(false)
                nameGeneratorTester.runAllTests()

            } catch (e: Exception) {
                Log.e(TAG, "초기화 중 오류 발생", e)
                showError("초기화 중 오류가 발생 했습니다: ${e.message}")
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