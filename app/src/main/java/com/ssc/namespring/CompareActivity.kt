// CompareActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.ui.compare.CompareActivityComponents
import com.ssc.namespring.ui.compare.CompareActivityEventHandler
import com.ssc.namespring.ui.compare.CompareActivitySetupHelper

class CompareActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CompareActivity"
    }

    private lateinit var components: CompareActivityComponents
    private lateinit var eventHandler: CompareActivityEventHandler
    private lateinit var setupHelper: CompareActivitySetupHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)

        initializeActivity()
    }

    private fun initializeActivity() {
        // 컴포넌트 초기화
        components = CompareActivityComponents(this)
        components.initialize()

        // 이벤트 핸들러 초기화
        eventHandler = CompareActivityEventHandler(this, components)

        // 설정 헬퍼 초기화
        setupHelper = CompareActivitySetupHelper(this, components, eventHandler)

        // UI 설정
        setupHelper.setupToolbar()
        setupHelper.setupRecyclerViews()
        setupHelper.setupInteractions()
        setupHelper.setupObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_compare, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (eventHandler.onMenuItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}