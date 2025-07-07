// MainActivity.kt
package com.ssc.namespring

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.usecase.MainMagager
import com.ssc.namespring.ui.main.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainMagager
    private lateinit var activityInitializer: MainActivityInitializer
    private lateinit var navigationHelper: MainNavigationHelper
    private lateinit var buttonHandlerManager: MainButtonHandlerManager
    private lateinit var taskProgressObserver: MainTaskProgressObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainMagager()
        navigationHelper = MainNavigationHelper(this)

        if (!viewModel.hasCurrentProfile()) {
            navigationHelper.navigateToProfileList()
            return
        }

        setContentView(R.layout.activity_main)

        // 초기화 위임
        activityInitializer = MainActivityInitializer(this, viewModel)
        val components = activityInitializer.initialize()

        // 버튼 핸들러 설정
        buttonHandlerManager = MainButtonHandlerManager(
            this, 
            viewModel, 
            components.uiComponents,
            navigationHelper
        )
        buttonHandlerManager.setupButtonListeners()

        // 작업 진행 상태 관찰자 설정
        taskProgressObserver = MainTaskProgressObserver(
            this,
            components.uiComponents
        )

        viewModel.getCurrentProfile()?.let { profile ->
            taskProgressObserver.observeTaskProgress(profile.id)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProfile()
        if (!viewModel.hasCurrentProfile()) {
            navigationHelper.navigateToProfileList()
        } else {
            viewModel.getCurrentProfile()?.let { profile ->
                taskProgressObserver.observeTaskProgress(profile.id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        buttonHandlerManager.cleanup()
    }
}