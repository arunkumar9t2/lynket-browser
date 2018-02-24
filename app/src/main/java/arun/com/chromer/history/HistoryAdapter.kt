/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
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

package arun.com.chromer.history

import android.app.Activity
import android.database.Cursor
import android.support.v7.util.AsyncListUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.Chromer
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.glide.GlideRequests
import butterknife.BindView
import butterknife.ButterKnife
import timber.log.Timber

/**
 * Created by Arunkumar on 06-03-2017.
 */
internal class HistoryAdapter(
        activity: Activity,
        private val linearLayoutManager: LinearLayoutManager
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val asyncWebsiteList: AsyncListUtil<Website>
    private var cursor: Cursor? = null
    private val glideRequests: GlideRequests
    private var tabsManager: DefaultTabsManager

    private val dataCallback = object : AsyncListUtil.DataCallback<Website>() {
        override fun refreshData(): Int {
            Timber.d("Refresh data")
            return if (cursor != null && !cursor!!.isClosed) {
                cursor!!.count
            } else 0
        }

        override fun fillData(data: Array<Website>, startPosition: Int, itemCount: Int) {
            if (cursor != null) {
                for (i in 0 until itemCount) {
                    cursor!!.moveToPosition(startPosition + i)
                    data[i] = Website.fromCursor(cursor!!)
                }
            }
        }
    }

    private val viewCallback = object : AsyncListUtil.ViewCallback() {
        override fun getItemRangeInto(outRange: IntArray) {
            outRange[0] = linearLayoutManager.findFirstVisibleItemPosition()
            outRange[1] = linearLayoutManager.findLastVisibleItemPosition()
        }

        override fun onDataRefresh() {
            Timber.d("onDataRefresh")
            notifyDataSetChanged()
        }

        override fun onItemLoaded(position: Int) {
            Timber.d("onItemLoaded, position %d", position)
            notifyItemChanged(position)
        }
    }

    init {
        asyncWebsiteList = AsyncListUtil(Website::class.java, 50, dataCallback, viewCallback)
        glideRequests = GlideApp.with(activity)
        tabsManager = (activity.application as Chromer).appComponent.defaultTabsManager()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_history_list_item_template, parent, false))
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val website = asyncWebsiteList.getItem(position)
        holder.bind(website)
    }

    override fun onViewRecycled(holder: HistoryViewHolder?) {
        super.onViewRecycled(holder)
        glideRequests.clear(holder?.historyFavicon)
    }

    override fun getItemCount(): Int {
        return asyncWebsiteList.itemCount
    }

    fun getItemAt(position: Int): Website? {
        return asyncWebsiteList.getItem(position)
    }

    fun setCursor(cursor: Cursor?) {
        if (this.cursor != null && this.cursor != cursor) {
            try {
                this.cursor?.close()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        this.cursor = cursor
        refresh()
    }

    fun onRangeChanged() {
        asyncWebsiteList.onRangeChanged()
    }

    private fun refresh() {
        asyncWebsiteList.refresh()
    }

    fun cleanUp() {
        cursor?.close()
    }

    internal inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.history_title)
        @JvmField
        var historyTitle: TextView? = null
        @BindView(R.id.history_favicon)
        @JvmField
        var historyFavicon: ImageView? = null
        @BindView(R.id.history_subtitle)
        @JvmField
        var historySubtitle: TextView? = null
        @BindView(R.id.history_amp)
        @JvmField
        var historyAmp: ImageView? = null

        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener {
                val position = adapterPosition
                val website = asyncWebsiteList.getItem(position)
                if (website != null && position != RecyclerView.NO_POSITION) {
                    tabsManager.openUrl(itemView.context, website)
                }
            }

            historyAmp?.setOnClickListener {
                val position = adapterPosition
                val website = asyncWebsiteList.getItem(position)
                if (website != null && position != RecyclerView.NO_POSITION) {
                    tabsManager.openUrl(itemView.context, Website.Ampify(website))
                }
            }
        }

        fun bind(website: Website?) {
            if (website == null) {
                historyTitle?.setText(R.string.loading)
                historySubtitle?.setText(R.string.loading)
                historyFavicon?.setImageDrawable(null)
                historyAmp?.visibility = GONE
                glideRequests.clear(historyFavicon!!)
            } else {
                historyTitle?.text = website.safeLabel()
                historySubtitle?.text = website.preferredUrl()
                GlideApp.with(itemView.context)
                        .load(website)
                        .into(historyFavicon!!)

                if (website.hasAmp()) {
                    historyAmp?.show()
                } else {
                    historyAmp?.gone()
                }
            }
        }
    }
}