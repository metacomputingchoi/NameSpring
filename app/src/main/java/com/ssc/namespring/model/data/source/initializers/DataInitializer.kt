// model/data/source/initializers/DataInitializer.kt
package com.ssc.namespring.model.data.source.initializers

import android.content.Context
import android.util.Log
import com.ssc.namespring.model.domain.entity.SurnameData
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataInitializer(
    private val nameDataService: INameDataService
) {
    companion object {
        private const val TAG = "DataInitializer"
    }

    suspend fun initializeData(
        context: Context,
        onProgress: suspend (Int, String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            onProgress(0, "데이터 로딩 시작...")
        }

        withContext(Dispatchers.Main) {
            onProgress(25, "이름 데이터 로딩 중...")
        }

        try {
            nameDataService.init(context)
            Log.d(TAG, "NameData 초기화 성공")
        } catch (e: Exception) {
            Log.e(TAG, "NameData 초기화 실패", e)
            throw Exception("이름 데이터 초기화 실패: ${e.message}")
        }

        withContext(Dispatchers.Main) {
            onProgress(50, "성씨 데이터 로딩 중...")
        }

        try {
            SurnameData.init(context)
            Log.d(TAG, "SurnameData 초기화 성공")
        } catch (e: Exception) {
            Log.e(TAG, "SurnameData 초기화 실패", e)
            Log.w(TAG, "성씨 데이터 로드 실패했지만 계속 진행")
        }

        withContext(Dispatchers.Main) {
            onProgress(75, "데이터 검증 중...")
        }
    }
}