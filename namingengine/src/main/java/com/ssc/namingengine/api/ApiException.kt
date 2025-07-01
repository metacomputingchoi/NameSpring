// com/ssc/namingengine/api/ApiException.kt
package com.ssc.namingengine.api

class ApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    companion object {
        fun fromNamingException(e: com.ssc.namingengine.exception.NamingException): ApiException {
            return ApiException(e.message ?: "이름 생성 중 오류 발생", e)
        }

        fun fromException(e: Exception): ApiException {
            return ApiException("예상치 못한 오류 발생: ${e.message}", e)
        }
    }
}