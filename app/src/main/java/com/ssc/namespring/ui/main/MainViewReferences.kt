// ui/main/MainViewReferences.kt
package com.ssc.namespring.ui.main

import android.app.Activity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ssc.namespring.R

class MainViewReferences(activity: Activity) {
    val rootLayout: ConstraintLayout = activity.findViewById(R.id.rootLayout)
    val tvProfileLabel: TextView = activity.findViewById(R.id.tvProfileLabel)
    val tvScore: TextView = activity.findViewById(R.id.tvScore)
    val tvScoreIcon: TextView = activity.findViewById(R.id.tvScoreIcon)
    val scoreContainer: LinearLayout = activity.findViewById(R.id.scoreContainer)
    val tvName: TextView = activity.findViewById(R.id.tvName)
    val tvBirthInfo: TextView = activity.findViewById(R.id.tvBirthInfo)
    val tvOhaengInfo: TextView = activity.findViewById(R.id.tvOhaengInfo)

    val ohaengContainers: List<LinearLayout> = listOf(
        activity.findViewById(R.id.containerWood),
        activity.findViewById(R.id.containerFire),
        activity.findViewById(R.id.containerEarth),
        activity.findViewById(R.id.containerMetal),
        activity.findViewById(R.id.containerWater)
    )

    val ohaengCounts: List<TextView> = listOf(
        activity.findViewById(R.id.tvWoodCount),
        activity.findViewById(R.id.tvFireCount),
        activity.findViewById(R.id.tvEarthCount),
        activity.findViewById(R.id.tvMetalCount),
        activity.findViewById(R.id.tvWaterCount)
    )

    val btnNaming: CardView = activity.findViewById(R.id.btnNaming)
    val btnEvaluation: CardView = activity.findViewById(R.id.btnEvaluation)
    val btnCompare: CardView = activity.findViewById(R.id.btnCompare)
    val btnHistory: CardView = activity.findViewById(R.id.btnHistory)

    val serviceButtons: List<CardView> = listOf(btnNaming, btnEvaluation, btnCompare, btnHistory)
}