// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namingengine.NamingEngine
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.controller.NameGeneratorController
import com.ssc.namespring.model.NameGeneratorModel
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.NameGeneratorView

class MainActivity : AppCompatActivity(), NameGeneratorView {

    private lateinit var namingEngine: NamingEngine
    private lateinit var model: NameGeneratorModel
    private lateinit var controller: NameGeneratorController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        initializeComponents()
    }

    private fun initializeComponents() {
        try {
            namingEngine = NamingEngine.create(
                logger = AndroidLogger("NamingEngine")
            )
            model = NameGeneratorModel(namingEngine)
            controller = NameGeneratorController(model, this)

            showToast("초기화 완료")
        } catch (e: Exception) {
            showError("초기화 실패: ${e.message}")
        }
    }

    override fun showLoading(isLoading: Boolean) {
        showToast(if (isLoading) "로딩 중..." else "로딩 완료")
    }

    override fun showResults(names: List<GeneratedName>, elapsedTime: Long) {
        showToast("${names.size}개 생성 (${elapsedTime}ms)")
    }

    override fun showError(message: String) {
        showToast("❌ $message", Toast.LENGTH_LONG)
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}