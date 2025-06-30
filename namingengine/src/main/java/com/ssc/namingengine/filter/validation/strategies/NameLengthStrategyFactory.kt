// model/filter/validation/strategies/NameLengthStrategyFactory.kt
package com.ssc.namingengine.filter.validation.strategies

object NameLengthStrategyFactory {

    fun getStrategy(surLength: Int, nameLength: Int): NameLengthStrategy {
        return NameLengthStrategyRegistry.getStrategy(surLength, nameLength)
    }
}