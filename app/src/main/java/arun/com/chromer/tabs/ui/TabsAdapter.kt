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

package arun.com.chromer.tabs.ui

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.R
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.glide.GlideRequests
import butterknife.BindView
import butterknife.ButterKnife
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription

/**
 * Created by arunk on 07-03-2017.
 */
class TabsAdapter
constructor(val gldieRequests: GlideRequests) : RecyclerView.Adapter<TabsAdapter.TabsViewHolder>() {
    private val tabs: ArrayList<TabsManager.Tab> = ArrayList()
    private val tabsReceiver: PublishSubject<List<TabsManager.Tab>> = PublishSubject.create()
    private val subs = CompositeSubscription()

    init {
        setHasStableIds(true)
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

    override fun onViewRecycled(holder: TabsViewHolder?) {
        super.onViewRecycled(holder)
        gldieRequests.clear(holder?.icon)
    }

    fun cleanUp() {
        subs.clear()
    }

    fun setTabs(tabs: List<TabsManager.Tab>) {
        tabsReceiver.onNext(tabs)
    }


    inner class TabsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.icon)
        @JvmField
        var icon: ImageView? = null
        @BindView(R.id.label)
        @JvmField
        var label: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(tab: TabsManager.Tab) {
            if (tab.website != null) {
                label?.text = tab.website?.safeLabel()
                gldieRequests.load(tab.website).into(icon)
            } else {
                // label?.text = tab.url
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
            return oldList[oldItemPosition].url == newList[newItemPosition].url
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return isEquals(oldItemPosition, newItemPosition)
        }

        private fun isEquals(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
