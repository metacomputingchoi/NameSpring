// MainActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.ProfileManager

class MainActivity : AppCompatActivity() {

    // 서비스 매핑을 위한 데이터 클래스
    data class ServiceIndex(val btnId: Int, val clazz: Class<out AppCompatActivity>)

    // 서비스 버튼 매핑 테이블
    private val services = listOf(
        ServiceIndex(R.id.service_button_naming, NamingActivity::class.java),
        ServiceIndex(R.id.service_button_evaluation, EvaluationActivity::class.java),
        ServiceIndex(R.id.service_button_compare, CompareActivity::class.java),
        ServiceIndex(R.id.service_button_history, HistoryActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 현재 프로필이 없으면 프로필 리스트로 이동
        if (ProfileManager.getCurrentProfile() == null) {
            navigateToProfileList()
            return
        }

        setContentView(R.layout.main_layout)
        init()
    }

    private fun init() {
        displayProfileInfo()
        setupServiceButtons()
    }

    private fun displayProfileInfo() {
        ProfileManager.getCurrentProfile()?.let { profile ->
            findViewById<TextView>(R.id.tvName).text = profile.getFullName()
            findViewById<TextView>(R.id.tvBirthDate).text = profile.getBirthDateString()
        }
    }

    private fun setupServiceButtons() {
        services.forEach { (btnId, activityClass) ->
            findViewById<View>(btnId).setOnClickListener {
                startActivity(Intent(this, activityClass))
            }
        }
    }

    private fun navigateToProfileList() {
        startActivity(Intent(this, ProfileListActivity::class.java))
        finish()
    }
}