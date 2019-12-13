package com.ygoular.notes.model

import com.ygoular.notes.BaseApplication
import com.ygoular.notes.R
import com.ygoular.notes.helper.PrefsHelper
import com.ygoular.notes.helper.PrefsHelper.PREF_SORTING_STRATEGY
import com.ygoular.notes.helper.PrefsHelper.PREF_SORTING_STRATEGY_DEFAULT

enum class SortingStrategy(val value: String) {
    PRIORITY(BaseApplication.mApplicationComponent.application().getString(R.string.higher_priority)),
    MODIFICATION(BaseApplication.mApplicationComponent.application().getString(R.string.last_modified));

    companion object {
        private val map = values().associateBy(SortingStrategy::value)
        fun fromValue(value: String): SortingStrategy = map[value] ?: PREF_SORTING_STRATEGY_DEFAULT

        fun fetchSortingStrategy(): SortingStrategy {
            return fromValue(
                PrefsHelper[PREF_SORTING_STRATEGY, PREF_SORTING_STRATEGY_DEFAULT.value].toString()
            )
        }
    }
}