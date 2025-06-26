// model/exception/NamingException.kt
package com.ssc.namespring.model.exception

sealed class NamingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class InvalidInputException(
        message: String,
        val input: String? = null,
        cause: Throwable? = null
    ) : NamingException("Invalid input: $message${input?.let { " (input: '$it')" } ?: ""}", cause)

    class DataNotFoundException(
        message: String,
        val dataType: String? = null,
        val searchKey: String? = null,
        cause: Throwable? = null
    ) : NamingException(
        "Data not found: $message${dataType?.let { " (type: $it)" } ?: ""}${searchKey?.let { " (key: '$it')" } ?: ""}", 
        cause
    )

    class HanjaException(
        message: String,
        val hanja: String? = null,
        cause: Throwable? = null
    ) : NamingException("Hanja error: $message${hanja?.let { " (hanja: '$it')" } ?: ""}", cause)

    class FilteringException(
        message: String,
        val filterName: String? = null,
        cause: Throwable? = null
    ) : NamingException("Filtering failed: $message${filterName?.let { " (filter: $it)" } ?: ""}", cause)

    class ConfigurationException(
        message: String,
        cause: Throwable? = null
    ) : NamingException("Configuration error: $message", cause)
}
