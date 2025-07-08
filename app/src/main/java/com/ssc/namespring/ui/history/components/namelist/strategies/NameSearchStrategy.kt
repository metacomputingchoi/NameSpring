// ui/history/components/namelist/strategies/NameSearchStrategy.kt
package com.ssc.namespring.ui.history.components.namelist.strategies

interface NameSearchStrategy {
    fun matches(name: String, query: String): Boolean
    fun getPriority(): Int
}