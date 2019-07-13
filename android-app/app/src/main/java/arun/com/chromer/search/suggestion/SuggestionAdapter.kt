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
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import arun.com.chromer.R
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.search.suggestion.items.SuggestionItem
import butterknife.BindView
import butterknife.ButterKnife
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

/**
 * Created by Arun on 03/08/2016.
 */
class SuggestionAdapter(
        context: Context
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionItemHolder>() {
    private val searchIcon: Drawable
    private val historyIcon: Drawable
    private val copyIcon: Drawable
    private val suggestionItems = ArrayList<SuggestionItem>()
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private val clicks = PublishSubject.create<SuggestionItem>()

    fun clicks(): Observable<SuggestionItem> = clicks.asObservable()

    init {
        setHasStableIds(true)
        searchIcon = IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_magnify)
                .color(ContextCompat.getColor(context, R.color.material_dark_light))
                .sizeDp(18)
        historyIcon = IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_history)
                .color(ContextCompat.getColor(context, R.color.md_red_500))
                .sizeDp(18)
        copyIcon = IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_content_copy)
                .color(ContextCompat.getColor(context, R.color.md_green_500))
                .sizeDp(18)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionItemHolder {
        return SuggestionItemHolder(layoutInflater.inflate(R.layout.widget_suggestions_item_template, parent, false))
    }

    override fun onBindViewHolder(holder: SuggestionItemHolder, position: Int) {
        val suggestionItem = suggestionItems[position]
        holder.bind(suggestionItem)
    }

    override fun getItemCount(): Int {
        return suggestionItems.size
    }

    override fun getItemId(position: Int): Long {
        return suggestionItems[position].hashCode().toLong()
    }

    fun updateSuggestions(newSuggestions: List<SuggestionItem>) {
        val suggestionDiff = SuggestionDiff(suggestionItems, newSuggestions)
        val diffResult = DiffUtil.calculateDiff(suggestionDiff, true)
        suggestionItems.clear()
        suggestionItems.addAll(newSuggestions)
        diffResult.dispatchUpdatesTo(this)
    }

    fun clear() {
        suggestionItems.clear()
        notifyDataSetChanged()
    }

    inner class SuggestionItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        @BindView(R.id.suggestions_text)
        @JvmField
        var suggestion: TextView? = null
        @BindView(R.id.suggestions_sub_title)
        @JvmField
        var suggestionSubTitle: TextView? = null
        @BindView(R.id.suggestion_icon)
        @JvmField
        var icon: ImageView? = null

        init {
            ButterKnife.bind(this, view)
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    clicks.onNext(suggestionItems[position])
                }
            }
        }

        fun bind(suggestionItem: SuggestionItem) {
            suggestion?.text = suggestionItem.title
            when (suggestionItem.type) {
                SuggestionItem.COPY -> icon?.setImageDrawable(copyIcon)
                SuggestionItem.HISTORY -> icon?.setImageDrawable(historyIcon)
                SuggestionItem.GOOGLE -> icon?.setImageDrawable(searchIcon)
            }
            when {
                TextUtils.isEmpty(suggestionItem.subTitle) -> {
                    suggestionSubTitle?.gone()
                    suggestionSubTitle?.text = null
                }
                else -> {
                    suggestionSubTitle?.show()
                    suggestionSubTitle?.text = suggestionItem.subTitle
                }
            }
        }
    }

    private inner class SuggestionDiff internal constructor(
            private val oldList: List<SuggestionItem>,
            private val newList: List<SuggestionItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return isEquals(oldItemPosition, newItemPosition)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return isEquals(oldItemPosition, newItemPosition)
        }

        private fun isEquals(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
