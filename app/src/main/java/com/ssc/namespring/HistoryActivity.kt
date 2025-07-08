// HistoryActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.ui.history.*
import com.ssc.namespring.model.presentation.adapter.TaskHistoryAdapter

class HistoryActivity : AppCompatActivity(), 
    TaskHistoryAdapter.SelectionListener,
    HistorySelectionManager.SelectionModeListener {

    private lateinit var coordinator: HistoryActivityCoordinator
    private lateinit var menuHandler: HistoryActivityMenuHandler
    private lateinit var lifecycleHandler: HistoryActivityLifecycleHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val profileId = intent.getStringExtra("profile_id")

        // Coordinator를 통한 초기화
        coordinator = HistoryActivityCoordinator(this, profileId)
        coordinator.initialize(this, this)

        // Menu와 Lifecycle 핸들러 초기화
        menuHandler = HistoryActivityMenuHandler(
            this,
            coordinator.selectionManager,
            coordinator.viewModel
        )
        lifecycleHandler = HistoryActivityLifecycleHandler(this, coordinator)

        // UI 설정 및 데이터 관찰
        lifecycleHandler.setupViews()
        lifecycleHandler.observeData(this)
    }

    // TaskHistoryAdapter.SelectionListener 구현
    override fun onSelectionChanged(selectedCount: Int) {
        coordinator.selectionManager.updateSelectionUI(selectedCount)
    }

    // HistorySelectionManager.SelectionModeListener 구현
    override fun onSelectionModeChanged(isSelectionMode: Boolean) {
        coordinator.adapter.isSelectionMode = isSelectionMode
        invalidateOptionsMenu()
    }

    // 메뉴 관련 메서드들은 MenuHandler에 위임
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return menuHandler.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuHandler.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!menuHandler.handleBackPress()) {
            super.onBackPressed()
        }
    }
}
