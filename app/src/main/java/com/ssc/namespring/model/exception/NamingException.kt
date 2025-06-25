// model/exception/NamingException.kt
package com.ssc.namespring.model.exception

open class NamingException(message: String) : Exception(message) {
    class InvalidInputException(message: String) : NamingException(message)
    class DataNotFoundException(message: String) : NamingException(message)
}
