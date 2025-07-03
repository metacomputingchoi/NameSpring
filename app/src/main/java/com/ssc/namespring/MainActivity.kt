// MainActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ssc.namespring.model.business.MainViewModel
import com.ssc.namespring.model.business.MainUiState
import com.ssc.namespring.utils.ViewUtils

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    // UI Components
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var tvProfileLabel: TextView
    private lateinit var tvScore: TextView
    private lateinit var ivScoreIcon: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvBirthInfo: TextView
    private lateinit var tvOhaengInfo: TextView
    private lateinit var ohaengContainers: List<LinearLayout>
    private lateinit var ohaengCounts: List<TextView>
    private lateinit var serviceButtons: List<CardView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel()
        if (!viewModel.hasCurrentProfile()) {
            navigateToProfileList()
            return
        }

        setContentView(R.layout.activity_main)
        initViews()
        observeViewModel()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        tvProfileLabel = findViewById(R.id.tvProfileLabel)
        tvScore = findViewById(R.id.tvScore)
        ivScoreIcon = findViewById(R.id.ivScoreIcon)
        tvName = findViewById(R.id.tvName)
        tvBirthInfo = findViewById(R.id.tvBirthInfo)
        tvOhaengInfo = findViewById(R.id.tvOhaengInfo)

        ohaengContainers = listOf(
            findViewById(R.id.containerWood),
            findViewById(R.id.containerFire),
            findViewById(R.id.containerEarth),
            findViewById(R.id.containerMetal),
            findViewById(R.id.containerWater)
        )

        ohaengCounts = listOf(
            findViewById(R.id.tvWoodCount),
            findViewById(R.id.tvFireCount),
            findViewById(R.id.tvEarthCount),
            findViewById(R.id.tvMetalCount),
            findViewById(R.id.tvWaterCount)
        )

        serviceButtons = listOf(
            findViewById<CardView>(R.id.btnNaming).apply {
                setOnClickListener { startActivity(Intent(this@MainActivity, NamingActivity::class.java)) }
            },
            findViewById<CardView>(R.id.btnEvaluation).apply {
                setOnClickListener { startActivity(Intent(this@MainActivity, EvaluationActivity::class.java)) }
            },
            findViewById<CardView>(R.id.btnCompare).apply {
                setOnClickListener { startActivity(Intent(this@MainActivity, CompareActivity::class.java)) }
            },
            findViewById<CardView>(R.id.btnHistory).apply {
                setOnClickListener { startActivity(Intent(this@MainActivity, HistoryActivity::class.java)) }
            }
        )
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: MainUiState) {
        tvProfileLabel.text = state.profileName
        tvScore.text = state.scoreText
        tvName.text = state.fullName
        tvBirthInfo.text = state.birthInfo
        tvOhaengInfo.text = state.ohaengInfo

        state.ohaengCounts.forEachIndexed { index, count ->
            ohaengCounts[index].text = count.toString()
        }

        ViewUtils.applyTheme(this, rootLayout, ivScoreIcon, state.theme)
        ViewUtils.applyOhaengTheme(this, ohaengContainers, state.theme, state.ohaengCounts)
    }

    private fun navigateToProfileList() {
        startActivity(Intent(this, ProfileListActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProfile()
        if (!viewModel.hasCurrentProfile()) {
            navigateToProfileList()
        }
    }
}
