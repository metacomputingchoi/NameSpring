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
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.ViewUtils

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    // UI Components
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var tvProfileLabel: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvScoreIcon: TextView
    private lateinit var scoreContainer: LinearLayout
    private lateinit var tvName: TextView
    private lateinit var tvBirthInfo: TextView
    private lateinit var tvOhaengInfo: TextView
    private lateinit var ohaengContainers: List<LinearLayout>
    private lateinit var ohaengCounts: List<TextView>
    private lateinit var serviceButtons: List<CardView>
    private lateinit var btnNaming: CardView
    private lateinit var btnEvaluation: CardView
    private lateinit var btnCompare: CardView
    private lateinit var btnHistory: CardView

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
        tvScoreIcon = findViewById(R.id.tvScoreIcon)
        scoreContainer = findViewById(R.id.scoreContainer)
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

        // 서비스 버튼 초기화
        btnNaming = findViewById(R.id.btnNaming)
        btnEvaluation = findViewById(R.id.btnEvaluation)
        btnCompare = findViewById(R.id.btnCompare)
        btnHistory = findViewById(R.id.btnHistory)

        // 버튼 클릭 리스너 설정
        btnNaming.setOnClickListener {
            startActivity(Intent(this@MainActivity, NamingActivity::class.java))
        }
        btnEvaluation.setOnClickListener {
            startActivity(Intent(this@MainActivity, EvaluationActivity::class.java))
        }
        btnCompare.setOnClickListener {
            startActivity(Intent(this@MainActivity, CompareActivity::class.java))
        }
        btnHistory.setOnClickListener {
            startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
        }
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

        // 테마별 이모지 설정
        val scoreEmoji = when (state.theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> getString(R.string.icon_flower_full)
            Profile.ScoreTheme.WARM_SPRING -> getString(R.string.icon_sprout_bloom)
            Profile.ScoreTheme.CLOUDY_SPRING -> getString(R.string.icon_sprout)
            Profile.ScoreTheme.RAINY_SPRING -> getString(R.string.icon_seed)
            Profile.ScoreTheme.COLD_SPRING -> getString(R.string.icon_dormant_seed)
            Profile.ScoreTheme.NOT_EVALUATED -> getString(R.string.icon_dormant_seed)
        }
        tvScoreIcon.text = scoreEmoji

        // 점수 컨테이너 배경색 적용
        applyScoreContainerTheme(state.theme)

        // 나머지 테마 적용
        ViewUtils.applyTheme(this, rootLayout, tvScoreIcon, state.theme)
        ViewUtils.applyOhaengTheme(this, ohaengContainers, state.theme, state.ohaengCounts)

        updateServiceButtonsTheme(state.theme)
    }

    private fun updateServiceButtonsTheme(theme: Profile.ScoreTheme) {
        val buttonAlpha = when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING,
            Profile.ScoreTheme.WARM_SPRING -> 0.7f  // 밝은 테마는 70%
            else -> 0.8f  // 어두운 테마는 80%
        }

        listOf<CardView>(btnNaming, btnEvaluation, btnCompare, btnHistory).forEach { button ->
            button.alpha = buttonAlpha
        }
    }

    private fun applyScoreContainerTheme(theme: Profile.ScoreTheme) {
        val accentColor = when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> R.color.sunny_spring_accent
            Profile.ScoreTheme.WARM_SPRING -> R.color.warm_spring_accent
            Profile.ScoreTheme.CLOUDY_SPRING -> R.color.cloudy_spring_accent
            Profile.ScoreTheme.RAINY_SPRING -> R.color.rainy_spring_accent
            Profile.ScoreTheme.COLD_SPRING -> R.color.cold_spring_accent
            Profile.ScoreTheme.NOT_EVALUATED -> R.color.not_evaluated_accent
        }

        scoreContainer.backgroundTintList = getColorStateList(accentColor)

        // 점수 텍스트 색상도 조정 (선택사항)
        val scoreTextColor = when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING,
            Profile.ScoreTheme.WARM_SPRING -> R.color.white
            else -> R.color.text_primary
        }
        tvScore.setTextColor(getColor(scoreTextColor))
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
