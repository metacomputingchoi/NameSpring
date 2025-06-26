// model/filter/strategy/NameLengthStrategyFactory.kt
package com.ssc.namespring.model.filter.strategy

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.filter.strategy.impl.*

object NameLengthStrategyFactory {

    fun getStrategy(surLength: Int, nameLength: Int): NameLengthStrategy {
        val lengthPair = surLength to nameLength

        return when (lengthPair) {
            NamingCalculationConstants.NameLengthCombinations.SINGLE_SINGLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_SINGLE ->
                SingleCharNameStrategy()

            NamingCalculationConstants.NameLengthCombinations.SINGLE_DOUBLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_DOUBLE ->
                DoubleCharNameStrategy()

            NamingCalculationConstants.NameLengthCombinations.SINGLE_TRIPLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_TRIPLE ->
                TripleCharNameStrategy()

            NamingCalculationConstants.NameLengthCombinations.SINGLE_QUAD,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_QUAD ->
                QuadCharNameStrategy()

            else -> DefaultNameStrategy()
        }
    }
}