// exception/NamingEngineException.kt
package com.ssc.namingengine.exception

class NamingEngineException(
    message: String,
    cause: Throwable? = null
) : Exception("NamingEngine error: $message", cause)