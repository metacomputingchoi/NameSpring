// ui/history/helpers/HistoryTabHelper.kt
package com.ssc.namespring.ui.history.helpers

import com.google.android.material.tabs.TabLayout
import com.ssc.namespring.ui.history.HistoryFilterManager

object HistoryTabHelper {
    fun setupTabs(tabLayout: TabLayout, onTabSelected: (HistoryFilterManager.TaskFilter) -> Unit) {
        tabLayout.addTab(tabLayout.newTab().setText("전체"))
        tabLayout.addTab(tabLayout.newTab().setText("완료"))
        tabLayout.addTab(tabLayout.newTab().setText("활성/대기"))
        tabLayout.addTab(tabLayout.newTab().setText("취소"))
        tabLayout.addTab(tabLayout.newTab().setText("실패"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val filter = when (tab.position) {
                    1 -> HistoryFilterManager.TaskFilter.COMPLETED
                    2 -> HistoryFilterManager.TaskFilter.ACTIVE_QUEUE
                    3 -> HistoryFilterManager.TaskFilter.CANCELLED
                    4 -> HistoryFilterManager.TaskFilter.FAILED
                    else -> HistoryFilterManager.TaskFilter.ALL
                }
                onTabSelected(filter)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}