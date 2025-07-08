// ui/splash/SplashProgressManager.kt
package com.ssc.namespring.ui.splash

import android.annotation.SuppressLint
import android.widget.ProgressBar
import android.widget.TextView

class SplashProgressManager(
    private val progressBar: ProgressBar,
    private val tvProgress: TextView,
    private val tvStatus: TextView
) {
    @SuppressLint("SetTextI18n")
    fun updateProgress(progress: Int, message: String) {
        progressBar.progress = progress
        tvProgress.text = "$progress%"
        tvStatus.text = message
    }
}