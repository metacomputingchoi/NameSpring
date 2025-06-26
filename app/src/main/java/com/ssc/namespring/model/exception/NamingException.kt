// model/exception/NamingException.kt
package com.ssc.namespring.model.exception

// 피드백: Exception 고도화는 나중에 필요시 추가
open class NamingException(message: String) : Exception(message) {
    class InvalidInputException(message: String) : NamingException(message)
    class DataNotFoundException(message: String) : NamingException(message)
}
