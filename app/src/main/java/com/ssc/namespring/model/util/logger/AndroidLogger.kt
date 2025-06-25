// model/util/logger/AndroidLogger.kt
package com.ssc.namespring.model.util.logger

import android.util.Log

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
