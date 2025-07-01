// view/NameGeneratorView.kt
package com.ssc.namespring.view

import com.ssc.namingengine.data.GeneratedName

interface NameGeneratorView {
    fun showLoading(isLoading: Boolean)
    fun showResults(names: List<GeneratedName>, elapsedTime: Long)
    fun showError(message: String)
}