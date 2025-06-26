// model/util/logger/Logger.kt
package com.ssc.namespring.model.util.logger

interface Logger {
    fun d(message: String)
    fun e(message: String, throwable: Throwable? = null)
    fun v(message: String)
}
