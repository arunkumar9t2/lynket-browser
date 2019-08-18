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

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

const val COPY = -1
const val GOOGLE = 0
const val HISTORY = 1

sealed class SuggestionItem(
        open val title: String,
        open val subTitle: String? = null,
        val type: Int = GOOGLE
) {
    data class CopySuggestionItem(
            override val title: String,
            override val subTitle: String
    ) : SuggestionItem(title, subTitle, COPY)

    data class GoogleSuggestionItem(
            override val title: String,
            override val subTitle: String? = null
    ) : SuggestionItem(title, subTitle, GOOGLE)

    data class HistorySuggestionItem(
            override val title: String,
            override val subTitle: String? = null
    ) : SuggestionItem(title, subTitle, HISTORY)

    companion object {

        object SuggestionItemDiffCallback : DiffUtil.ItemCallback<SuggestionItem>() {

            override fun areItemsTheSame(
                    oldItem: SuggestionItem,
                    newItem: SuggestionItem
            ) = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                    oldItem: SuggestionItem,
                    newItem: SuggestionItem
            ) = oldItem == newItem
        }
    }
}
