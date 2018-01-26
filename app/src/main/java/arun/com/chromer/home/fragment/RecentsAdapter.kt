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

package arun.com.chromer.home.fragment

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.scopes.PerFragment
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.glide.GlideApp
import butterknife.BindView
import butterknife.ButterKnife
import java.util.*
import javax.inject.Inject

/**
 * Created by arunk on 07-03-2017.
 */
@PerFragment
class RecentsAdapter @Inject
constructor(val tabsManager: DefaultTabsManager) : RecyclerView.Adapter<RecentsAdapter.RecentsViewHolder>() {
    private val websites = ArrayList<Website>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentsViewHolder {
        return RecentsViewHolder(tabsManager, LayoutInflater.from(parent.context).inflate(R.layout.activity_main_recents_item_template, parent, false))
    }

    override fun onBindViewHolder(holder: RecentsViewHolder, position: Int) {
        val website = websites[position]
        holder.bind(website)
    }

    override fun onViewDetachedFromWindow(holder: RecentsViewHolder) {
        super.onViewDetachedFromWindow(holder)
        GlideApp.with(holder.itemView.context).clear(holder.icon)
    }

    override fun getItemCount(): Int = websites.size

    override fun getItemId(position: Int): Long = websites[position].hashCode().toLong()

    internal fun setWebsites(websites: List<Website>) {
        val recentsDiff = WebsitesDiff(this.websites, websites)
        val diffUtil = DiffUtil.calculateDiff(recentsDiff)
        this.websites.clear()
        this.websites.addAll(websites)
        diffUtil.dispatchUpdatesTo(this)
    }

    class RecentsViewHolder(val tabsManager: DefaultTabsManager, itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.icon)
        @JvmField
        var icon: ImageView? = null
        @BindView(R.id.label)
        @JvmField
        var label: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(website: Website?) {
            if (website != null) {
                label?.text = website.safeLabel()
                itemView.setOnClickListener {
                    tabsManager.openUrl(itemView.context, website)
                }
                GlideApp.with(itemView.context)
                        .load(website)
                        .into(icon!!)
            }
        }
    }

    private inner class WebsitesDiff internal constructor(
            private val oldList: List<Website>,
            private val newList: List<Website>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

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
