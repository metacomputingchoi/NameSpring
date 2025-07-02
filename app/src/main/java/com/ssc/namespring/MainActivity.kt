// MainActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    lateinit var btnNaming: CardView
    lateinit var btnEvaluation: CardView
    lateinit var btnCompare: CardView
    lateinit var btnHistory: CardView

    // 1. 이럴때 inner class 구조체를 만든다.
    data class ServiceIndex(val btnId: Int, val clazz: Class<out AppCompatActivity>)
    // 2. 테이블을 "정의"한다.
    val services = listOf(
        ServiceIndex(R.id.service_button_naming, NamingActivity::class.java),
        ServiceIndex(R.id.service_button_evaluation, EvaluationActivity::class.java),
        ServiceIndex(R.id.service_button_compare, CompareActivity::class.java),
        ServiceIndex(R.id.service_button_history, HistoryActivity::class.java),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        init()
    }

    fun init() {
        services.forEach { (id, activity) -> findViewById<View>(id).setOnClickListener {
                startActivity(Intent(this, activity))
            }
        }
    }
}