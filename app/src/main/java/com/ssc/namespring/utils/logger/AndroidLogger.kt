// utils/logger/AndroidLogger.kt
package com.ssc.namespring.utils.logger

import android.util.Log
import com.ssc.namespring.model.util.logger.Logger

class AndroidLogger(private val tag: String) : Logger {
    override fun d(message: String) {
        Log.d(tag, message)
    }

    override fun e(message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }

    override fun v(message: String) {
        Log.v(tag, message)
    }
}