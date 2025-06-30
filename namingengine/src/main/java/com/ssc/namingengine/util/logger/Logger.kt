// model/util/logger/Logger.kt
package com.ssc.namingengine.util.logger

interface Logger {
    fun d(message: String)
    fun e(message: String, throwable: Throwable? = null)
    fun v(message: String)
}
