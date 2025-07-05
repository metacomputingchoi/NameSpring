// model/presentation/adapter/ProfileItemBinder.kt
package com.ssc.namespring.model.presentation.adapter

import android.annotation.SuppressLint
import android.view.View
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.OhaengInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.presentation.formatter.ProfileFormatter

class ProfileItemBinder(
    private val views: ProfileViewReferences
) {
    private val formatter = ProfileFormatter()
    private val themeApplier = ProfileThemeApplier()

    fun bindBasicInfo(profile: Profile) {
        views.tvProfileName.text = profile.profileName
        views.tvFullName.text = formatter.formatFullName(profile)
    }

    fun bindDateTime(profile: Profile) {
        views.tvBirthDate.text = formatter.formatBirthDate(profile.birthDate)
        views.tvBirthTime.text = formatter.formatBirthTime(profile.birthDate)
    }

    fun bindSajuInfo(profile: Profile) {
        profile.sajuInfo?.let { saju ->
            views.tvSaju.text = saju.fourPillars.joinToString(" ")
            views.tvSaju.visibility = View.VISIBLE
        } ?: run {
            views.tvSaju.visibility = View.GONE
        }
    }

    fun bindOhaengInfo(profile: Profile) {
        profile.ohaengInfo?.let { ohaeng ->
            bindOhaengDistribution(ohaeng)
            bindOhaengBalance(ohaeng)
        } ?: run {
            views.ohaengDistribution?.visibility = View.GONE
            views.tvOhaeng.text = "오행 정보 없음"
            views.tvOhaeng.visibility = View.VISIBLE
            views.tvOhaeng.setTextColor(views.tvOhaeng.context.getColor(R.color.text_secondary))
        }
    }

    fun bindScore(profile: Profile) {
        views.scoreContainer.visibility = View.VISIBLE

        if (profile.isEvaluated()) {
            views.tvScore.text = profile.nameBomScore.toString()
            themeApplier.applyTheme(views, profile)
        } else {
            themeApplier.applyNotEvaluatedTheme(views)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindOhaengDistribution(ohaeng: OhaengInfo) {
        views.tvWoodDist?.text = "木${ohaeng.wood}"
        views.tvFireDist?.text = "火${ohaeng.fire}"
        views.tvEarthDist?.text = "土${ohaeng.earth}"
        views.tvMetalDist?.text = "金${ohaeng.metal}"
        views.tvWaterDist?.text = "水${ohaeng.water}"
        views.ohaengDistribution?.visibility = View.VISIBLE
    }

    private fun bindOhaengBalance(ohaeng: OhaengInfo) {
        val lacking = ohaeng.getLackingOhaeng()
        val excessive = ohaeng.getExcessOhaeng()

        val ohaengText = formatter.formatOhaengBalance(lacking, excessive)
        views.tvOhaeng.text = ohaengText
        views.tvOhaeng.visibility = View.VISIBLE

        if (lacking.isNotEmpty()) {
            val color = getOhaengColor(lacking.first())
            views.tvOhaeng.setTextColor(views.tvOhaeng.context.getColor(color))
        }
    }

    private fun getOhaengColor(ohaeng: String): Int = when(ohaeng) {
        "목" -> R.color.ohaeng_wood
        "화" -> R.color.ohaeng_fire
        "토" -> R.color.ohaeng_earth
        "금" -> R.color.ohaeng_metal
        "수" -> R.color.ohaeng_water
        else -> R.color.text_secondary
    }
}