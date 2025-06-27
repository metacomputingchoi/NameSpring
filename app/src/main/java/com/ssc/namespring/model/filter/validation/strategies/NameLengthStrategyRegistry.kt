// model/filter/validation/strategies/NameLengthStrategyRegistry.kt
package com.ssc.namespring.model.filter.validation.strategies

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.filter.validation.strategies.impl.*

internal object NameLengthStrategyRegistry {
    private val strategies = mutableMapOf<Pair<Int, Int>, () -> NameLengthStrategy>()

    init {
        registerStrategy(
            NamingCalculationConstants.NameLengthCombinations.SINGLE_SINGLE,
            ::SingleCharNameStrategy
        )
        registerStrategy(
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_SINGLE,
            ::SingleCharNameStrategy
        )
        registerStrategy(
            NamingCalculationConstants.NameLengthCombinations.SINGLE_DOUBLE,
            ::DoubleCharNameStrategy
        )
        registerStrategy(
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_DOUBLE,
            ::DoubleCharNameStrategy
        )
        registerStrategy(
            NamingCalculationConstants.NameLengthCombinations.SINGLE_TRIPLE,
            ::TripleCharNameStrategy
        )
        registerStrategy(
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_TRIPLE,
            ::TripleCharNameStrategy
        )
        registerStrategy(
            NamingCalculationConstants.NameLengthCombinations.SINGLE_QUAD,
            ::QuadCharNameStrategy
        )
        registerStrategy(
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_QUAD,
            ::QuadCharNameStrategy
        )
    }

    fun registerStrategy(lengthPair: Pair<Int, Int>, strategyFactory: () -> NameLengthStrategy) {
        strategies[lengthPair] = strategyFactory
    }

    fun getStrategy(surLength: Int, nameLength: Int): NameLengthStrategy {
        return strategies[surLength to nameLength]?.invoke() ?: DefaultNameStrategy()
    }
}