// model/domain/service/evaluation/interfaces/IScoreCalculator.kt
package com.ssc.namespring.model.domain.service.evaluation.interfaces

interface IScoreCalculator {
    fun calculate(data: Any): Int
    fun getName(): String
}
