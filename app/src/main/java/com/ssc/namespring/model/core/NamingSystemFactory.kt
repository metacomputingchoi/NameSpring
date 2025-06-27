// model/core/NamingSystemFactory.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.filter.*
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.service.*
import com.ssc.namespring.model.util.logger.Logger

internal class NamingSystemFactory(
    private val dataRepository: DataRepository,
    private val logger: Logger
) {
    fun createServices(): Services {
        val hanjaRepository = HanjaRepository(dataRepository)
        val cacheManager = CacheManager()

        val nameParser = NameParser()
        val surnameValidator = SurnameValidator(dataRepository)
        val sajuCalculator = SajuCalculator(dataRepository)
        val baleumOhaengCalculator = BaleumOhaengCalculator(cacheManager)
        val multiOhaengHarmonyAnalyzer = MultiOhaengHarmonyAnalyzer(cacheManager)
        val hanjaHoeksuAnalyzer = HanjaHoeksuAnalyzer(dataRepository, hanjaRepository)
        val nameSuriAnalyzer = NameSuriAnalyzer(hanjaHoeksuAnalyzer, multiOhaengHarmonyAnalyzer)
        val analysisInfoGenerator = AnalysisInfoGenerator(baleumOhaengCalculator, multiOhaengHarmonyAnalyzer)

        val nameGenerator = NameGenerator(
            hanjaRepository,
            nameSuriAnalyzer,
            hanjaHoeksuAnalyzer,
            multiOhaengHarmonyAnalyzer
        )

        return Services(
            hanjaRepository = hanjaRepository,
            nameParser = nameParser,
            surnameValidator = surnameValidator,
            sajuCalculator = sajuCalculator,
            baleumOhaengCalculator = baleumOhaengCalculator,
            multiOhaengHarmonyAnalyzer = multiOhaengHarmonyAnalyzer,
            hanjaHoeksuAnalyzer = hanjaHoeksuAnalyzer,
            nameSuriAnalyzer = nameSuriAnalyzer,
            nameGenerator = nameGenerator,
            analysisInfoGenerator = analysisInfoGenerator
        )
    }

    fun createFilters(services: Services): List<NameFilterStrategy> {
        return listOf(
            BaleumOhaengEumyangFilter(
                services.baleumOhaengCalculator::getBaleumOhaeng,
                services.baleumOhaengCalculator::getBaleumEumyang,
                services.multiOhaengHarmonyAnalyzer::checkBaleumOhaengHarmony
            ),
            JawonOhaengFilter(),
            BaleumNaturalFilter { dataRepository.dictHangulGivenNames }
        )
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