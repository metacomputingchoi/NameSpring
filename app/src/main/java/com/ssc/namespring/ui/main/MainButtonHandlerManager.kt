// ui/main/MainButtonHandlerManager.kt
package com.ssc.namespring.ui.main

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.*
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.usecase.MainMagager

class MainButtonHandlerManager(
    private val activity: AppCompatActivity,
    private val viewModel: MainMagager,
    private val uiComponents: MainUIComponents,
    private val navigationHelper: MainNavigationHelper
) {
    private val handler = Handler(Looper.getMainLooper())
    private val buttonHandler = MainButtonHandler(activity, handler)

    fun setupButtonListeners() {
        uiComponents.btnNaming.setOnClickListener { view ->
            buttonHandler.handleButtonClick(view) {
                viewModel.getCurrentProfile()?.let { profile ->
                    val config = ProfileFormConfig(
                        mode = ProfileFormMode.NAMING,
                        parentProfileId = profile.id
                    )
                    val intent = ProfileFormActivity.newIntent(activity, config)
                    activity.startActivity(intent)
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        }

        uiComponents.btnEvaluation.setOnClickListener { view ->
            buttonHandler.handleButtonClick(view) {
                viewModel.getCurrentProfile()?.let { profile ->
                    val config = ProfileFormConfig(
                        mode = ProfileFormMode.EVALUATION,
                        parentProfileId = profile.id
                    )
                    val intent = ProfileFormActivity.newIntent(activity, config)
                    activity.startActivity(intent)
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        }

        uiComponents.btnCompare.setOnClickListener { view ->
            buttonHandler.handleButtonClick(view, 300) {
                val currentProfile = viewModel.getCurrentProfile()
                val intent = Intent(activity, CompareActivity::class.java).apply {
                    currentProfile?.let {
                        putExtra("initial_profile_id", it.id)
                    }
                }
                activity.startActivity(intent)
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        uiComponents.btnHistory.setOnClickListener { view ->
            buttonHandler.handleButtonClick(view, 300) {
                val currentProfile = viewModel.getCurrentProfile()
                val intent = Intent(activity, HistoryActivity::class.java).apply {
                    currentProfile?.let {
                        putExtra("profile_id", it.id)
                    }
                }
                activity.startActivity(intent)
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
    }
}