// model/domain/service/workmanager/workers/NamingWorker.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.ssc.namespring.model.domain.service.workmanager.BaseWorker
import com.ssc.namespring.model.domain.service.workmanager.WorkResult
import com.ssc.namespring.model.domain.service.workmanager.workers.naming.NamingInputParser
import com.ssc.namespring.model.domain.service.workmanager.workers.naming.NamingProcessor
import com.ssc.namespring.model.domain.service.workmanager.workers.naming.NamingResultBuilder

/**
 * 이름 생성 작업을 처리하는 Worker
 * SOLID 원칙에 따라 리팩토링됨
 */
class NamingWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params) {

    companion object {
        private const val TAG = "NamingWorker"
    }

    private val inputParser = NamingInputParser()
    private val processor = NamingProcessor()
    private val resultBuilder = NamingResultBuilder()

    override suspend fun performWork(): WorkResult {
        return try {
            Log.d(TAG, "Starting naming work for profile: $profileId")

            // 1. 입력 데이터 파싱
            val inputData = getInputDataMap()
            val parsedInput = inputParser.parse(inputData)
            updateProgress(20)

            // 2. 이름 생성
            val generatedNames = processor.generateNames(
                fullInput = parsedInput.fullInput,
                birthDateTime = parsedInput.birthDateTime,
                isYajaTime = parsedInput.isYajaTime
            )
            updateProgress(70)

            // 3. 결과 구성
            val resultData = resultBuilder.buildResult(generatedNames, parsedInput)
            updateProgress(90)

            // 4. Raw 데이터 직렬화
            val rawDataJson = resultBuilder.serializeRawData(generatedNames)
            updateProgress(100)

            WorkResult(
                success = true,
                data = resultData,
                rawData = rawDataJson
            )
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid input: ${e.message}", e)
            WorkResult(
                success = false,
                error = e.message ?: "Invalid input"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Naming failed", e)
            WorkResult(
                success = false,
                error = "Naming failed: ${e.message}"
            )
        }
    }
}