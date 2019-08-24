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

package arun.com.chromer.search.suggestion

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.search.suggestion.items.COPY
import arun.com.chromer.search.suggestion.items.GOOGLE
import arun.com.chromer.search.suggestion.items.HISTORY
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionItem.Companion.SuggestionItemDiffCallback
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.widget_suggestions_item_template.*

/**
 * Created by Arun on 03/08/2016.
 */
class SuggestionAdapter(
        context: Context
) : ListAdapter<SuggestionItem, SuggestionAdapter.SuggestionItemHolder>(SuggestionItemDiffCallback) {

    private val searchIcon: Drawable by lazy {
        IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_magnify)
                .color(ContextCompat.getColor(context, R.color.material_dark_light))
                .sizeDp(18)
    }

    private val historyIcon: Drawable by lazy {
        IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_history)
                .color(ContextCompat.getColor(context, R.color.md_red_500))
                .sizeDp(18)
    }

    private val copyIcon: Drawable by lazy {
        IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_content_copy)
                .color(ContextCompat.getColor(context, R.color.md_green_500))
                .sizeDp(18)
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private val clicks = PublishSubject.create<SuggestionItem>()

    fun clicks(): Observable<SuggestionItem> = clicks.hide()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionItemHolder {
        return SuggestionItemHolder(
                layoutInflater.inflate(
                        R.layout.widget_suggestions_item_template,
                        parent,
                        false
                ),
                ::getItem
        )
    }

    override fun onBindViewHolder(
            holder: SuggestionItemHolder,
            position: Int
    ) = holder.bind(getItem(position))

    fun clear() = submitList(emptyList())

    inner class SuggestionItemHolder(
            override val containerView: View,
            item: (position: Int) -> SuggestionItem
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            containerView.setOnClickListener {
                adapterPosition
                        .takeIf { adapterPosition != RecyclerView.NO_POSITION }
                        ?.let { position -> clicks.onNext(item(position)) }
            }
        }

        fun bind(suggestionItem: SuggestionItem) {
            suggestions_text.text = suggestionItem.title
            when (suggestionItem.type) {
                COPY -> suggestion_icon.setImageDrawable(copyIcon)
                HISTORY -> suggestion_icon.setImageDrawable(historyIcon)
                GOOGLE -> suggestion_icon.setImageDrawable(searchIcon)
            }
            when {
                TextUtils.isEmpty(suggestionItem.subTitle) -> {
                    suggestions_sub_title.gone()
                    suggestions_sub_title.text = null
                }
                else -> {
                    suggestions_sub_title.show()
                    suggestions_sub_title.text = suggestionItem.subTitle
                }
            }
        }
    }
}
