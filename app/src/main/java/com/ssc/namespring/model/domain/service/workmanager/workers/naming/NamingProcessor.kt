// model/domain/service/workmanager/workers/naming/NamingProcessor.kt
package com.ssc.namespring.model.domain.service.workmanager.workers.naming

import android.util.Log
import com.ssc.namespring.model.domain.service.factory.NamingEngineProvider
import com.ssc.namingengine.data.GeneratedName
import java.time.LocalDateTime

/**
 * 이름 생성 엔진 처리 담당
 */
internal class NamingProcessor {
    companion object {
        private const val TAG = "NamingProcessor"
    }

    private val namingEngine = NamingEngineProvider.getInstance()

    fun generateNames(
        fullInput: String,
        birthDateTime: LocalDateTime,
        isYajaTime: Boolean
    ): List<GeneratedName> {
        Log.d(TAG, "Full naming input: $fullInput")

        return try {
            namingEngine.generateNames(
                userInput = fullInput,
                birthDateTime = birthDateTime,
                useYajasi = isYajaTime,
                verbose = true,
                withoutFilter = false
            ).also {
                Log.d(TAG, "Generated ${it.size} names")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate names", e)
            throw RuntimeException("Failed to generate names: ${e.message}", e)
        }
    }
}