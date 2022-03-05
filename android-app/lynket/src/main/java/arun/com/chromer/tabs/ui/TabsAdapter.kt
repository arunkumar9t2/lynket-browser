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

package arun.com.chromer.tabs.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.tabs.*
import arun.com.chromer.util.glide.GlideRequests
import butterknife.ButterKnife
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_tabs_item_template.*

/**
 * Created by arunk on 07-03-2017.
 */
class TabsAdapter
constructor(
  val glideRequests: GlideRequests,
  val tabsManager: TabsManager
) : ListAdapter<TabsManager.Tab, TabsAdapter.TabsViewHolder>(TabDiff) {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ) = TabsViewHolder(
    LayoutInflater.from(parent.context).inflate(
      R.layout.fragment_tabs_item_template,
      parent,
      false
    ),
    ::getItem
  )

  override fun onBindViewHolder(
    holder: TabsViewHolder,
    position: Int
  ) = holder.bind(getItem(position))

  override fun onViewRecycled(holder: TabsViewHolder) {
    super.onViewRecycled(holder)
    glideRequests.clear(holder.icon)
  }

  fun getTabAt(adapterPosition: Int): TabsManager.Tab = getItem(adapterPosition)

  inner class TabsViewHolder(
    override val containerView: View,
    getItem: (Int) -> TabsManager.Tab
  ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    init {
      ButterKnife.bind(this, itemView)
      itemView.setOnClickListener {
        if (adapterPosition != RecyclerView.NO_POSITION) {
          val tab = getItem(adapterPosition)
          val url = tab.url
          tabsManager.reOrderTabByUrl(
            itemView.context,
            Website(url),
            listOf(tab.getTargetActivityName())
          )
        }
      }
    }

    fun bind(tab: TabsManager.Tab) {
      if (tab.website != null) {
        websiteTitle.text = tab.website?.safeLabel()
        glideRequests.load(tab.website).into(icon!!)
        websiteUrl.text = tab.website?.url
        when (tab.type) {
          WEB_VIEW, WEB_VIEW_EMBEDDED -> {
            websiteTabMode.setText(R.string.web_view)
            websiteTabModeIcon.setImageDrawable(
              IconicsDrawable(itemView.context)
                .icon(CommunityMaterial.Icon.cmd_web)
                .color(ContextCompat.getColor(itemView.context, R.color.md_blue_500))
                .sizeDp(16)
            )
          }
          CUSTOM_TAB -> {
            websiteTabMode.setText(R.string.custom_tab)
            websiteTabModeIcon.setImageDrawable(
              IconicsDrawable(itemView.context)
                .icon(CommunityMaterial.Icon.cmd_google_chrome)
                .color(ContextCompat.getColor(itemView.context, R.color.md_orange_500))
                .sizeDp(16)
            )
          }
          ARTICLE -> {
            websiteTabMode.setText(R.string.article_mode)
            websiteTabModeIcon.setImageDrawable(
              IconicsDrawable(itemView.context)
                .icon(CommunityMaterial.Icon.cmd_file_document)
                .color(ContextCompat.getColor(itemView.context, R.color.md_grey_700))
                .sizeDp(16)
            )
          }
        }
      } else {
        //  websiteTitle?.text = tab.url
      }
    }
  }

  private object TabDiff : DiffUtil.ItemCallback<TabsManager.Tab>() {

    override fun areItemsTheSame(
      oldItem: TabsManager.Tab,
      newItem: TabsManager.Tab
    ): Boolean = oldItem == newItem

    override fun areContentsTheSame(
      oldItem: TabsManager.Tab,
      newItem: TabsManager.Tab
    ): Boolean = oldItem == newItem
  }
}
