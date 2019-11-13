/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.search.suggestion.items

import arun.com.chromer.data.website.model.Website
import arun.com.chromer.search.suggestion.items.SuggestionType.*

enum class SuggestionType {
    COPY,
    GOOGLE,
    HISTORY
}

sealed class SuggestionItem(
        open val query: String,
        open val title: String,
        open val subTitle: String? = null,
        val type: SuggestionType = GOOGLE
) {
    data class CopySuggestionItem(
            override val query: String,
            override val title: String,
            override val subTitle: String
    ) : SuggestionItem(query, title, subTitle, COPY)

    data class GoogleSuggestionItem(
            override val query: String,
            override val title: String,
            override val subTitle: String? = null
    ) : SuggestionItem(query, title, subTitle, GOOGLE)

    data class HistorySuggestionItem(
            override val query: String,
            val website: Website,
            override val title: String,
            override val subTitle: String? = null
    ) : SuggestionItem(query, title, subTitle, HISTORY)
}
