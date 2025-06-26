// model/util/logger/PrintLogger.kt
package com.ssc.namespring.model.util.logger

class PrintLogger(private val tag: String) : Logger {
    override fun d(message: String) {
        println("[$tag] DEBUG: $message")
    }

    override fun e(message: String, throwable: Throwable?) {
        println("[$tag] ERROR: $message")
        throwable?.printStackTrace()
    }

    override fun v(message: String) {
        println("[$tag] VERBOSE: $message")
    }
}
