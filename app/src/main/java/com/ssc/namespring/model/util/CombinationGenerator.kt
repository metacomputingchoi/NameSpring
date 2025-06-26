// model/util/CombinationGenerator.kt
package com.ssc.namespring.model.util

object CombinationGenerator {

    fun <T, R> generateCombinations(
        candidateLists: List<List<T>>,
        createResult: (List<T>) -> R?
    ): Sequence<R> {
        return sequence {
            generateRecursive(candidateLists, 0, mutableListOf(), createResult)
                .filterNotNull()
                .forEach { yield(it) }
        }
    }

    private fun <T, R> generateRecursive(
        candidateLists: List<List<T>>,
        index: Int,
        current: MutableList<T>,
        createResult: (List<T>) -> R?
    ): Sequence<R?> {
        return sequence {
            if (index == candidateLists.size) {
                yield(createResult(current.toList()))
            } else {
                candidateLists[index].forEach { candidate ->
                    current.add(candidate)
                    generateRecursive(candidateLists, index + 1, current, createResult)
                        .forEach { yield(it) }
                    current.removeAt(current.size - 1)
                }
            }
        }
    }
}