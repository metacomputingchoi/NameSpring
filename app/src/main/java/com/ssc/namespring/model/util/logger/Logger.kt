// model/util/logger/Logger.kt
package com.ssc.namespring.model.util.logger

// 피드백: Logger 인터페이스는 model에 남겨서 플랫폼 독립적으로 유지
interface Logger {
    fun d(message: String)
    fun e(message: String, throwable: Throwable? = null)
    fun v(message: String)
}
