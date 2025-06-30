// core/NamingSystemFactory.kt
package com.ssc.namingengine.core

import com.ssc.namingengine.filter.*
import com.ssc.namingengine.repository.DataRepository
import com.ssc.namingengine.repository.HanjaRepository
import com.ssc.namingengine.service.*
import com.ssc.namingengine.util.logger.Logger

internal class NamingSystemFactory private constructor(
    private val dataRepository: DataRepository,
    private val logger: Logger
) {

    companion object {
        fun builder() = Builder()
    }

    fun createServices(): Services {
        return ServicesBuilder()
            .withDataRepository(dataRepository)
            .build()
    }

    fun createFilters(services: Services): List<NameFilterStrategy> {
        return FilterBuilder()
            .withBaleumOhaengEumyangFilter(
                services.baleumOhaengCalculator::getBaleumOhaeng,
                services.baleumOhaengCalculator::getBaleumEumyang,
                services.multiOhaengHarmonyAnalyzer::checkBaleumOhaengHarmony
            )
            .withJawonOhaengFilter()
            .withBaleumNaturalFilter { dataRepository.dictHangulGivenNames }
            .build()
    }

    class Builder {
        private lateinit var dataRepository: DataRepository
        private lateinit var logger: Logger

        fun withDataRepository(repository: DataRepository) = apply {
            this.dataRepository = repository
        }

        fun withLogger(logger: Logger) = apply {
            this.logger = logger
        }

        fun build() = NamingSystemFactory(dataRepository, logger)
    }

    private class ServicesBuilder {
        private lateinit var dataRepository: DataRepository

        fun withDataRepository(repository: DataRepository) = apply {
            this.dataRepository = repository
        }

        fun build(): Services {
            val hanjaRepository = HanjaRepository(dataRepository)
            val cacheManager = CacheManager()

            return Services(
                hanjaRepository = hanjaRepository,
                nameParser = NameParser(),
                surnameValidator = SurnameValidator(dataRepository),
                sajuCalculator = SajuCalculator(dataRepository),
                baleumOhaengCalculator = BaleumOhaengCalculator(cacheManager),
                multiOhaengHarmonyAnalyzer = MultiOhaengHarmonyAnalyzer(cacheManager),
                hanjaHoeksuAnalyzer = HanjaHoeksuAnalyzer(dataRepository, hanjaRepository),
                nameSuriAnalyzer = NameSuriAnalyzer(
                    HanjaHoeksuAnalyzer(dataRepository, hanjaRepository),
                    MultiOhaengHarmonyAnalyzer(cacheManager)
                ),
                nameGenerator = NameGenerator(
                    hanjaRepository,
                    NameSuriAnalyzer(
                        HanjaHoeksuAnalyzer(dataRepository, hanjaRepository),
                        MultiOhaengHarmonyAnalyzer(cacheManager)
                    ),
                    HanjaHoeksuAnalyzer(dataRepository, hanjaRepository),
                    MultiOhaengHarmonyAnalyzer(cacheManager)
                ),
                analysisInfoGenerator = AnalysisInfoGenerator(
                    BaleumOhaengCalculator(cacheManager),
                    MultiOhaengHarmonyAnalyzer(cacheManager)
                )
            )
        }
    }

    private class FilterBuilder {
        private val filters = mutableListOf<NameFilterStrategy>()

        fun withBaleumOhaengEumyangFilter(
            getBaleumOhaeng: (Char) -> String?,
            getBaleumEumyang: (Char) -> Int?,
            checkBaleumOhaengHarmony: (String) -> Boolean
        ) = apply {
            filters.add(
                BaleumOhaengEumyangFilter(
                    getBaleumOhaeng,
                    getBaleumEumyang,
                    checkBaleumOhaengHarmony
                )
            )
        }

        fun withJawonOhaengFilter() = apply {
            filters.add(JawonOhaengFilter())
        }

        fun withBaleumNaturalFilter(dictProvider: () -> Set<String>) = apply {
            filters.add(BaleumNaturalFilter(dictProvider))
        }

        fun build(): List<NameFilterStrategy> = filters.toList()
    }
}

internal data class Services(
    val hanjaRepository: HanjaRepository,
    val nameParser: NameParser,
    val surnameValidator: SurnameValidator,
    val sajuCalculator: SajuCalculator,
    val baleumOhaengCalculator: BaleumOhaengCalculator,
    val multiOhaengHarmonyAnalyzer: MultiOhaengHarmonyAnalyzer,
    val hanjaHoeksuAnalyzer: HanjaHoeksuAnalyzer,
    val nameSuriAnalyzer: NameSuriAnalyzer,
    val nameGenerator: NameGenerator,
    val analysisInfoGenerator: AnalysisInfoGenerator
)