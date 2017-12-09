/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

import android.content.Context

import arun.com.chromer.R
import arun.com.chromer.util.Utils

class CopySuggestionItem(private val context: Context) : SuggestionItem {

    private val title: String = Utils.getClipBoardText(context) ?: ""

    override fun getTitle(): String {
        return title
    }

    override fun getSubTitle(): String? {
        return context.getString(R.string.text_you_copied)
    }

    override fun getType(): Int {
        return SuggestionItem.COPY
    }
}
