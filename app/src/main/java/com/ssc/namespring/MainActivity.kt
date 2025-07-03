// com/ssc/namespring/MainActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ssc.namespring.model.Profile
import com.ssc.namespring.model.ProfileManager
import com.ssc.namespring.model.OhaengInfo

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: ConstraintLayout
    private lateinit var tvProfileLabel: TextView
    private lateinit var tvScore: TextView
    private lateinit var ivScoreIcon: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvBirthInfo: TextView

    // 오행 텍스트뷰들
    private lateinit var tvWoodCount: TextView
    private lateinit var tvFireCount: TextView
    private lateinit var tvEarthCount: TextView
    private lateinit var tvMetalCount: TextView
    private lateinit var tvWaterCount: TextView

    // 오행 라벨 텍스트뷰들
    private lateinit var tvWoodLabel: TextView
    private lateinit var tvFireLabel: TextView
    private lateinit var tvEarthLabel: TextView
    private lateinit var tvMetalLabel: TextView
    private lateinit var tvWaterLabel: TextView

    // 오행 한자 텍스트뷰들
    private lateinit var tvWoodHanja: TextView
    private lateinit var tvFireHanja: TextView
    private lateinit var tvEarthHanja: TextView
    private lateinit var tvMetalHanja: TextView
    private lateinit var tvWaterHanja: TextView

    // 오행 컨테이너들
    private lateinit var containerWood: LinearLayout
    private lateinit var containerFire: LinearLayout
    private lateinit var containerEarth: LinearLayout
    private lateinit var containerMetal: LinearLayout
    private lateinit var containerWater: LinearLayout

    private lateinit var tvOhaengInfo: TextView

    // 서비스 버튼들
    private lateinit var btnNaming: CardView
    private lateinit var btnEvaluation: CardView
    private lateinit var btnCompare: CardView
    private lateinit var btnHistory: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 현재 프로필이 없으면 프로필 리스트로 이동
        if (ProfileManager.getCurrentProfile() == null) {
            startActivity(Intent(this, ProfileListActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        initViews()
        updateProfileInfo()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        tvProfileLabel = findViewById(R.id.tvProfileLabel)
        tvScore = findViewById(R.id.tvScore)
        ivScoreIcon = findViewById(R.id.ivScoreIcon)
        tvName = findViewById(R.id.tvName)
        tvBirthInfo = findViewById(R.id.tvBirthInfo)

        // 오행 요소들
        containerWood = findViewById(R.id.containerWood)
        containerFire = findViewById(R.id.containerFire)
        containerEarth = findViewById(R.id.containerEarth)
        containerMetal = findViewById(R.id.containerMetal)
        containerWater = findViewById(R.id.containerWater)

        tvWoodCount = findViewById(R.id.tvWoodCount)
        tvFireCount = findViewById(R.id.tvFireCount)
        tvEarthCount = findViewById(R.id.tvEarthCount)
        tvMetalCount = findViewById(R.id.tvMetalCount)
        tvWaterCount = findViewById(R.id.tvWaterCount)

        tvWoodLabel = findViewById(R.id.tvWoodLabel)
        tvFireLabel = findViewById(R.id.tvFireLabel)
        tvEarthLabel = findViewById(R.id.tvEarthLabel)
        tvMetalLabel = findViewById(R.id.tvMetalLabel)
        tvWaterLabel = findViewById(R.id.tvWaterLabel)

        tvWoodHanja = findViewById(R.id.tvWoodHanja)
        tvFireHanja = findViewById(R.id.tvFireHanja)
        tvEarthHanja = findViewById(R.id.tvEarthHanja)
        tvMetalHanja = findViewById(R.id.tvMetalHanja)
        tvWaterHanja = findViewById(R.id.tvWaterHanja)

        tvOhaengInfo = findViewById(R.id.tvOhaengInfo)

        // 서비스 버튼들
        btnNaming = findViewById(R.id.btnNaming)
        btnEvaluation = findViewById(R.id.btnEvaluation)
        btnCompare = findViewById(R.id.btnCompare)
        btnHistory = findViewById(R.id.btnHistory)

        // 버튼 클릭 리스너
        btnNaming.setOnClickListener {
            startActivity(Intent(this, NamingActivity::class.java))
        }

        btnEvaluation.setOnClickListener {
            startActivity(Intent(this, EvaluationActivity::class.java))
        }

        btnCompare.setOnClickListener {
            startActivity(Intent(this, CompareActivity::class.java))
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun updateProfileInfo() {
        val profile = ProfileManager.getCurrentProfile() ?: return

        // 프로필 정보 표시
        tvProfileLabel.text = profile.profileName
        tvScore.text = profile.nameBomScore.toString()
        tvName.text = profile.getFullName()
        tvBirthInfo.text = profile.getBirthDateString()

        // 점수에 따른 테마 적용
        applyTheme(profile)

        // 오행 정보 표시 및 스타일 적용
        updateOhaengDisplay(profile)
    }

    private fun updateOhaengDisplay(profile: Profile) {
        profile.ohaengInfo?.let { ohaeng ->
            tvWoodCount.text = ohaeng.wood.toString()
            tvFireCount.text = ohaeng.fire.toString()
            tvEarthCount.text = ohaeng.earth.toString()
            tvMetalCount.text = ohaeng.metal.toString()
            tvWaterCount.text = ohaeng.water.toString()

            // 테마에 따른 오행 스타일 적용
            val theme = profile.getScoreThemeColor()
            applyOhaengTheme(theme, ohaeng)

            // 오행 상태 텍스트
            val lacking = ohaeng.getLackingOhaeng()
            val excess = ohaeng.getExcessOhaeng()

            val infoText = when {
                lacking.isNotEmpty() && excess.isNotEmpty() ->
                    "부족한 오행: ${lacking.joinToString(", ")} · 많은 오행: ${excess.joinToString(", ")}"
                lacking.isNotEmpty() ->
                    "부족한 오행: ${lacking.joinToString(", ")}"
                excess.isNotEmpty() ->
                    "많은 오행: ${excess.joinToString(", ")}"
                else -> "오행이 균형 잡혀 있습니다"
            }
            tvOhaengInfo.text = infoText
        }
    }

    private fun applyTheme(profile: Profile) {
        val theme = profile.getScoreThemeColor()

        when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_sunny_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_flower_full)
            }
            Profile.ScoreTheme.WARM_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_warm_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_sprout_bloom)
            }
            Profile.ScoreTheme.CLOUDY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_cloudy_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_sprout)
            }
            Profile.ScoreTheme.RAINY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_rainy_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_seed)
            }
            Profile.ScoreTheme.COLD_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_cold_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_dormant_seed)
            }
        }
    }

    private fun applyOhaengTheme(theme: Profile.ScoreTheme, ohaeng: OhaengInfo) {
        // 각 오행 컨테이너와 라벨
        val containers = listOf(
            Triple(containerWood, tvWoodLabel, tvWoodHanja),
            Triple(containerFire, tvFireLabel, tvFireHanja),
            Triple(containerEarth, tvEarthLabel, tvEarthHanja),
            Triple(containerMetal, tvMetalLabel, tvMetalHanja),
            Triple(containerWater, tvWaterLabel, tvWaterHanja)
        )

        // 각 오행 값
        val values = listOf(ohaeng.wood, ohaeng.fire, ohaeng.earth, ohaeng.metal, ohaeng.water)

        when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> {
                // 화창한 봄 - 밝고 화사한 색상
                containers.forEachIndexed { index, (container, label, hanja) ->
                    if (values[index] == 0) {
                        // 부족한 오행 - 연한 핑크 배경에 진한 색상 텍스트
                        container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_sunny)
                        setOhaengTextColor(index, label, hanja, true, theme)
                    } else {
                        // 일반 오행 - 흰 배경에 파스텔 색상
                        container.setBackgroundResource(R.drawable.bg_ohaeng_normal_sunny)
                        setOhaengTextColor(index, label, hanja, false, theme)
                    }
                }
            }

            Profile.ScoreTheme.WARM_SPRING -> {
                // 따뜻한 봄 - 따뜻한 색감
                containers.forEachIndexed { index, (container, label, hanja) ->
                    if (values[index] == 0) {
                        container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_warm)
                        setOhaengTextColor(index, label, hanja, true, theme)
                    } else {
                        container.setBackgroundResource(R.drawable.bg_ohaeng_normal_warm)
                        setOhaengTextColor(index, label, hanja, false, theme)
                    }
                }
            }

            Profile.ScoreTheme.CLOUDY_SPRING -> {
                // 흐린 봄 - 차분한 색상
                containers.forEachIndexed { index, (container, label, hanja) ->
                    if (values[index] == 0) {
                        container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_cloudy)
                        setOhaengTextColor(index, label, hanja, true, theme)
                    } else {
                        container.setBackgroundResource(R.drawable.bg_ohaeng_normal_cloudy)
                        setOhaengTextColor(index, label, hanja, false, theme)
                    }
                }
            }

            Profile.ScoreTheme.RAINY_SPRING -> {
                // 비내리는 봄 - 차가운 색감
                containers.forEachIndexed { index, (container, label, hanja) ->
                    if (values[index] == 0) {
                        container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_rainy)
                        setOhaengTextColor(index, label, hanja, true, theme)
                    } else {
                        container.setBackgroundResource(R.drawable.bg_ohaeng_normal_rainy)
                        setOhaengTextColor(index, label, hanja, false, theme)
                    }
                }
            }

            Profile.ScoreTheme.COLD_SPRING -> {
                // 쌀쌀한 봄 - 차가운 색상
                containers.forEachIndexed { index, (container, label, hanja) ->
                    if (values[index] == 0) {
                        container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_cold)
                        setOhaengTextColor(index, label, hanja, true, theme)
                    } else {
                        container.setBackgroundResource(R.drawable.bg_ohaeng_normal_cold)
                        setOhaengTextColor(index, label, hanja, false, theme)
                    }
                }
            }
        }
    }

    private fun setOhaengTextColor(index: Int, label: TextView, hanja: TextView, isLacking: Boolean, theme: Profile.ScoreTheme) {
        val colors = when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> {
                // 화창한 봄 - 밝은 색상
                if (isLacking) {
                    listOf(
                        R.color.ohaeng_wood_dark,
                        R.color.ohaeng_fire_dark,
                        R.color.ohaeng_earth_dark,
                        R.color.ohaeng_metal_dark,
                        R.color.ohaeng_water_dark
                    )
                } else {
                    listOf(
                        R.color.ohaeng_wood_sunny,
                        R.color.ohaeng_fire_sunny,
                        R.color.ohaeng_earth_sunny,
                        R.color.ohaeng_metal_sunny,
                        R.color.ohaeng_water_sunny
                    )
                }
            }
            Profile.ScoreTheme.WARM_SPRING -> {
                // 따뜻한 봄
                if (isLacking) {
                    listOf(
                        R.color.ohaeng_wood_dark,
                        R.color.ohaeng_fire_dark,
                        R.color.ohaeng_earth_dark,
                        R.color.ohaeng_metal_dark,
                        R.color.ohaeng_water_dark
                    )
                } else {
                    listOf(
                        R.color.ohaeng_wood_warm,
                        R.color.ohaeng_fire_warm,
                        R.color.ohaeng_earth_warm,
                        R.color.ohaeng_metal_warm,
                        R.color.ohaeng_water_warm
                    )
                }
            }
            Profile.ScoreTheme.CLOUDY_SPRING -> {
                // 흐린 봄 - 중간 톤
                if (isLacking) {
                    listOf(
                        R.color.ohaeng_wood_dark,
                        R.color.ohaeng_fire_dark,
                        R.color.ohaeng_earth_dark,
                        R.color.ohaeng_metal_dark,
                        R.color.ohaeng_water_dark
                    )
                } else {
                    listOf(
                        R.color.ohaeng_wood_medium,
                        R.color.ohaeng_fire_medium,
                        R.color.ohaeng_earth_medium,
                        R.color.ohaeng_metal_medium,
                        R.color.ohaeng_water_medium
                    )
                }
            }
            Profile.ScoreTheme.RAINY_SPRING, Profile.ScoreTheme.COLD_SPRING -> {
                // 비오는/차가운 봄 - 진한 색상
                listOf(
                    R.color.ohaeng_wood_cold,
                    R.color.ohaeng_fire_cold,
                    R.color.ohaeng_earth_cold,
                    R.color.ohaeng_metal_cold,
                    R.color.ohaeng_water_cold
                )
            }
        }

        label.setTextColor(getColor(colors[index]))
        hanja.setTextColor(getColor(colors[index]))
    }

    override fun onResume() {
        super.onResume()
        // 프로필이 변경되었을 수 있으므로 다시 확인
        if (ProfileManager.getCurrentProfile() == null) {
            startActivity(Intent(this, ProfileListActivity::class.java))
            finish()
        } else {
            updateProfileInfo()
        }
    }
}