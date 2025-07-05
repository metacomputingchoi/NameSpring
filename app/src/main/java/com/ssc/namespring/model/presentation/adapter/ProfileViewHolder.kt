// model/presentation/adapter/ProfileViewHolder.kt
package com.ssc.namespring.model.presentation.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.Profile

class ProfileViewHolder(
    itemView: View,
    private val onItemClick: (Profile) -> Unit,
    private val onItemLongClick: (Profile) -> Boolean,
    private val menuHandler: ProfileMenuHandler
) : RecyclerView.ViewHolder(itemView) {

    private val views = ProfileViewReferences(itemView)
    private val binder = ProfileItemBinder(views)

    fun bind(profile: Profile, selectionManager: SelectionModeManager) {
        binder.bindBasicInfo(profile)
        binder.bindDateTime(profile)
        binder.bindSajuInfo(profile)
        binder.bindOhaengInfo(profile)
        binder.bindScore(profile)

        setupInteractions(profile, selectionManager)
    }

    private fun setupInteractions(profile: Profile, selectionManager: SelectionModeManager) {
        // 선택 모드 처리
        views.checkBox?.let { cb ->
            if (selectionManager.isSelectionMode()) {
                cb.visibility = View.VISIBLE
                cb.setOnCheckedChangeListener(null)
                cb.isChecked = selectionManager.isSelected(profile.id)

                cb.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != selectionManager.isSelected(profile.id)) {
                        onItemClick(profile)
                    }
                }
                views.btnMenu?.visibility = View.GONE
            } else {
                cb.visibility = View.GONE
                views.btnMenu?.visibility = View.VISIBLE
            }
        }

        // 클릭 이벤트
        views.cardView.setOnClickListener { view ->
            if (!selectionManager.isSelectionMode() || view !is CheckBox) {
                onItemClick(profile)
            }
        }

        views.cardView.setOnLongClickListener {
            onItemLongClick(profile)
        }

        views.btnMenu?.setOnClickListener {
            menuHandler.showMenu(it, profile)
        }
    }
}

data class ProfileViewReferences(
    val cardView: View,
    val checkBox: CheckBox?,
    val tvProfileName: TextView,
    val tvFullName: TextView,
    val tvBirthDate: TextView,
    val tvBirthTime: TextView,
    val tvSaju: TextView,
    val tvOhaeng: TextView,
    val tvSproutIcon: TextView,
    val tvScore: TextView,
    val btnMenu: ImageButton?,
    val scoreContainer: View,
    val tvWoodDist: TextView?,
    val tvFireDist: TextView?,
    val tvEarthDist: TextView?,
    val tvMetalDist: TextView?,
    val tvWaterDist: TextView?,
    val ohaengDistribution: LinearLayout?
) {
    constructor(itemView: View) : this(
        cardView = itemView.findViewById(R.id.cardView),
        checkBox = itemView.findViewById(R.id.checkBox),
        tvProfileName = itemView.findViewById(R.id.tvProfileName),
        tvFullName = itemView.findViewById(R.id.tvFullName),
        tvBirthDate = itemView.findViewById(R.id.tvBirthDate),
        tvBirthTime = itemView.findViewById(R.id.tvBirthTime),
        tvSaju = itemView.findViewById(R.id.tvSaju),
        tvOhaeng = itemView.findViewById(R.id.tvOhaeng),
        tvSproutIcon = itemView.findViewById(R.id.tvSproutIcon),
        tvScore = itemView.findViewById(R.id.tvScore),
        btnMenu = itemView.findViewById(R.id.btnMenu),
        scoreContainer = itemView.findViewById(R.id.scoreContainer),
        tvWoodDist = itemView.findViewById(R.id.tvWoodDist),
        tvFireDist = itemView.findViewById(R.id.tvFireDist),
        tvEarthDist = itemView.findViewById(R.id.tvEarthDist),
        tvMetalDist = itemView.findViewById(R.id.tvMetalDist),
        tvWaterDist = itemView.findViewById(R.id.tvWaterDist),
        ohaengDistribution = itemView.findViewById(R.id.ohaengDistribution)
    )
}