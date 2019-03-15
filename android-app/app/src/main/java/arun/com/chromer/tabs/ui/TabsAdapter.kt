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

package arun.com.chromer.tabs.ui

import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.tabs.*
import arun.com.chromer.util.glide.GlideRequests
import butterknife.BindView
import butterknife.ButterKnife
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription

/**
 * Created by arunk on 07-03-2017.
 */
class TabsAdapter
constructor(
        val glideRequests: GlideRequests,
        val tabsManager: DefaultTabsManager
) : RecyclerView.Adapter<TabsAdapter.TabsViewHolder>() {

    private val tabs: ArrayList<TabsManager.Tab> = ArrayList()
    private val tabsReceiver: PublishSubject<List<TabsManager.Tab>> = PublishSubject.create()
    private val subs = CompositeSubscription()

    init {
        setupReceiver()
    }

    private fun setupReceiver() {
        subs.add(tabsReceiver
                // .observeOn(Schedulers.io())
                .map {
                    val diff = DiffUtil.calculateDiff(TabsDiff(tabs, it))
                    tabs.clear()
                    tabs.addAll(it)
                    diff
                }
                // .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { it.dispatchUpdatesTo(this) }
                .subscribe())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabsViewHolder {
        return TabsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_tabs_item_template, parent, false))
    }

    override fun onBindViewHolder(holder: TabsViewHolder, position: Int) {
        holder.bind(tabs[position])
    }

    override fun getItemCount(): Int = tabs.size

    override fun getItemId(position: Int): Long = tabs.hashCode().toLong()

    override fun onViewRecycled(holder: TabsViewHolder) {
        super.onViewRecycled(holder)
        glideRequests.clear(holder.icon)
    }

    fun cleanUp() {
        subs.clear()
    }

    fun setTabs(tabs: List<TabsManager.Tab>) {
        tabsReceiver.onNext(tabs)
    }

    fun getTabAt(adapterPosition: Int): TabsManager.Tab = tabs[adapterPosition]

    inner class TabsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.icon)
        @JvmField
        var icon: ImageView? = null
        @BindView(R.id.websiteTitle)
        @JvmField
        var websiteTitle: TextView? = null
        @BindView(R.id.websiteTabMode)
        @JvmField
        var websiteTabMode: TextView? = null
        @BindView(R.id.websiteTabModeIcon)
        @JvmField
        var websiteTabModeIcon: ImageView? = null
        @BindView(R.id.websiteUrl)
        @JvmField
        var websiteUrl: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val tab = tabs[adapterPosition]
                    val url = tab.url
                    tabsManager.reOrderTabByUrl(itemView.context, Website(url), listOf(tab.getTargetActivityName()))
                }
            }
        }

        fun bind(tab: TabsManager.Tab) {
            if (tab.website != null) {
                websiteTitle?.text = tab.website?.safeLabel()
                glideRequests.load(tab.website).into(icon)
                websiteUrl?.text = tab.website?.url

                when (tab.type) {
                    WEB_VIEW -> {
                        websiteTabMode?.setText(R.string.web_view)
                        websiteTabModeIcon?.setImageDrawable(IconicsDrawable(itemView.context)
                                .icon(CommunityMaterial.Icon.cmd_web)
                                .color(ContextCompat.getColor(itemView.context, R.color.md_blue_500))
                                .sizeDp(16))
                    }
                    CUSTOM_TAB -> {
                        websiteTabMode?.setText(R.string.custom_tab)
                        websiteTabModeIcon?.setImageDrawable(IconicsDrawable(itemView.context)
                                .icon(CommunityMaterial.Icon.cmd_google_chrome)
                                .color(ContextCompat.getColor(itemView.context, R.color.md_orange_500))
                                .sizeDp(16))
                    }
                    ARTICLE -> {
                        websiteTabMode?.setText(R.string.article_mode)
                        websiteTabModeIcon?.setImageDrawable(IconicsDrawable(itemView.context)
                                .icon(CommunityMaterial.Icon.cmd_file_document)
                                .color(ContextCompat.getColor(itemView.context, R.color.md_grey_700))
                                .sizeDp(16))
                    }
                }
            } else {
                //  websiteTitle?.text = tab.url
            }
        }
    }

    private inner class TabsDiff internal constructor(
            private val oldList: List<TabsManager.Tab>,
            private val newList: List<TabsManager.Tab>
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
