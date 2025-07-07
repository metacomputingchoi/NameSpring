// ui/compare/CompareResultDialog.kt
package com.ssc.namespring.ui.compare

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.ui.compare.adapter.CompareResultPagerAdapter

class CompareResultDialog(
    context: Context,
    private val comparisonList: List<FavoriteName>
) : Dialog(context, android.R.style.Theme_Material_Light_NoActionBar) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_compare_result)

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupViews()
    }

    private fun setupViews() {
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        // ViewPager 어댑터 설정
        val adapter = CompareResultPagerAdapter(comparisonList)
        viewPager.adapter = adapter

        // TabLayout 연결
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val favorite = comparisonList[position]
            tab.text = favorite.fullNameKorean
        }.attach()

        // 닫기 버튼
        findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        // 전체 비교 보기 버튼
        findViewById<MaterialButton>(R.id.btnShowFullComparison).setOnClickListener {
            showFullComparison()
        }
    }

    private fun showFullComparison() {
        // 전체 비교 뷰 표시
        val containerComparison = findViewById<LinearLayout>(R.id.containerFullComparison)
        containerComparison.removeAllViews()

        comparisonList.forEach { favorite ->
            val itemView = layoutInflater.inflate(
                R.layout.item_compare_result_summary,
                containerComparison,
                false
            )

            // 각 항목 설정
            itemView.findViewById<TextView>(R.id.tvName).text =
                "${favorite.fullNameKorean} (${favorite.fullNameHanja})"

            try {
                val gson = com.google.gson.Gson()
                val generatedName = gson.fromJson(
                    favorite.jsonData,
                    com.ssc.namingengine.data.GeneratedName::class.java
                )

                // 주요 정보 표시
                val analysisInfo = generatedName.analysisInfo
                itemView.findViewById<TextView>(R.id.tvScore).text =
                    "총점: ${analysisInfo?.totalScore ?: 0}점"

                itemView.findViewById<TextView>(R.id.tvSagyeok).text =
                    "사격: ${generatedName.sagyeok.hyeong}, ${generatedName.sagyeok.won}, " +
                            "${generatedName.sagyeok.i}, ${generatedName.sagyeok.jeong}"

                itemView.findViewById<TextView>(R.id.tvOhaeng).text =
                    "오행: ${analysisInfo?.ohaengInfo?.baleumOhaeng ?: ""}"

            } catch (e: Exception) {
                // 에러 처리
            }

            containerComparison.addView(itemView)
        }
    }
}