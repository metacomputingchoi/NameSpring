// model/domain/service/profile/ProfileSearchService.kt
package com.ssc.namespring.model.domain.service.profile

import com.ssc.namespring.model.common.utils.MixedPatternUtils
import com.ssc.namespring.model.domain.entity.Profile

class ProfileSearchService {

    fun search(profiles: List<Profile>, query: String): List<Profile> {
        val lowercaseQuery = query.lowercase()
        val searchStrategy = determineSearchStrategy(query)

        return profiles.filter { profile ->
            searchStrategy.matches(profile, query, lowercaseQuery)
        }
    }

    private fun determineSearchStrategy(query: String): ProfileSearchStrategy {
        return when {
            MixedPatternUtils.containsMixedPattern(query) -> ProfileMixedPatternSearchStrategy()
            query.matches(Regex("^[ㄱ-ㅎ]+$")) -> ProfileChosungSearchStrategy()
            else -> GeneralSearchStrategy()
        }
    }

    private interface ProfileSearchStrategy {
        fun matches(profile: Profile, query: String, lowercaseQuery: String): Boolean
    }

    private class ProfileMixedPatternSearchStrategy : ProfileSearchStrategy {
        override fun matches(profile: Profile, query: String, lowercaseQuery: String): Boolean {
            return MixedPatternUtils.matchMixedPattern(profile.profileName, query) ||
                    MixedPatternUtils.matchMixedPattern(profile.getFullName(), query)
        }
    }

    private class ProfileChosungSearchStrategy : ProfileSearchStrategy {
        override fun matches(profile: Profile, query: String, lowercaseQuery: String): Boolean {
            return MixedPatternUtils.matchChosungPattern(profile.profileName, query) ||
                    MixedPatternUtils.matchChosungPattern(profile.getFullName(), query)
        }
    }

    private class GeneralSearchStrategy : ProfileSearchStrategy {
        override fun matches(profile: Profile, query: String, lowercaseQuery: String): Boolean {
            return profile.profileName.lowercase().contains(lowercaseQuery) ||
                    profile.getFullName().lowercase().contains(lowercaseQuery) ||
                    profile.getFullNameWithHanja().lowercase().contains(lowercaseQuery) ||
                    profile.getBirthDateString().contains(query)
        }
    }
}