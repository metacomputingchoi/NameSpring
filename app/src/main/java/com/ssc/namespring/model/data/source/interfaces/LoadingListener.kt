// model/data/source/interfaces/LoadingListener.kt
package com.ssc.namespring.model.data.source.interfaces

interface LoadingListener {
    fun onProgress(progress: Int, message: String)
    fun onComplete()
    fun onError(error: String)
}