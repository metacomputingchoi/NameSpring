// ui/compare/CompareTabManager.kt
package com.ssc.namespring.ui.compare

import com.google.android.material.tabs.TabLayout
import com.ssc.namespring.ui.compare.adapter.CompareSourceAdapter

class CompareTabManager(
    private val tabLayout: TabLayout,
    private val viewModel: CompareViewModel,
    private val sourceAdapter: CompareSourceAdapter
) {
    fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("즐겨찾기"))
        tabLayout.addTab(tabLayout.newTab().setText("삭제됨"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val showDeleted = tab.position == 1
                viewModel.setShowDeleted(showDeleted)
                sourceAdapter.setShowDeleted(showDeleted)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}