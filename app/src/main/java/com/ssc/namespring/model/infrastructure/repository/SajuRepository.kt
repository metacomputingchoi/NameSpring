// model/infrastructure/repository/SajuRepository.kt
package com.ssc.namespring.model.infrastructure.repository

import com.ssc.namespring.model.infrastructure.data.YMDRecord

interface SajuRepository {
    fun findByDate(year: Int, month: Int, day: Int): YMDRecord?
}