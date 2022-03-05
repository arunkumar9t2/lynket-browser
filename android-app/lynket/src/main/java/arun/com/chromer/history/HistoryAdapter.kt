/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.history

import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.inflate
import arun.com.chromer.extenstions.show
import arun.com.chromer.tabs.TabsManager
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.RequestManager
import dev.arunkumar.android.dagger.fragment.PerFragment
import javax.inject.Inject

/**
 * History adapter to render paged [Website] items.
 */
@PerFragment
class HistoryAdapter
@Inject
constructor(
  private val defaultTabsManager: TabsManager,
  private val requestManager: RequestManager
) : PagedListAdapter<Website, HistoryAdapter.HistoryViewHolder>(Website.DIFFER) {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    position: Int
  ): HistoryViewHolder =
    HistoryViewHolder(parent.inflate(R.layout.activity_history_list_item_template))

  override fun onBindViewHolder(
    historyViewHolder: HistoryViewHolder,
    position: Int
  ) = historyViewHolder.bind(getItem(position))

  fun getItemAt(adapterPosition: Int) = getItem(adapterPosition)

  /**
   * History ViewHolder to render history items
   */
  inner class HistoryViewHolder(
    itemView: View
  ) : RecyclerView.ViewHolder(itemView) {
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
        val website = getItem(position)
        if (website != null && position != RecyclerView.NO_POSITION) {
          defaultTabsManager.openUrl(itemView.context, website)
        }
      }

      historyAmp?.setOnClickListener {
        val position = adapterPosition
        val website = getItem(position)
        if (website != null && position != RecyclerView.NO_POSITION) {
          defaultTabsManager.openUrl(itemView.context, Website.Ampify(website))
        }
      }
    }

    fun bind(website: Website?) {
      if (website == null) {
        historyTitle?.setText(R.string.loading)
        historySubtitle?.setText(R.string.loading)
        historyFavicon?.setImageDrawable(null)
        historyAmp?.visibility = GONE
        requestManager.clear(historyFavicon!!)
      } else {
        historyTitle?.text = website.safeLabel()
        historySubtitle?.text = website.preferredUrl()
        requestManager
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
