// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.NameAnalyzer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        val analyzer = NameAnalyzer(this)
        analyzer.runTestCases()
    }
}