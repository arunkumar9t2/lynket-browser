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

package arun.com.chromer.home.epoxycontroller.model

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.tabs.TabsManager
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.layout_tabs_info_card.*

@EpoxyModelClass(layout = R.layout.layout_tabs_info_card)
abstract class TabsInfoModel : KotlinEpoxyModelWithHolder<TabsInfoModel.ViewHolder>() {
  @EpoxyAttribute
  lateinit var tabs: List<TabsManager.Tab>

  @EpoxyAttribute(DoNotHash)
  lateinit var tabsManager: TabsManager

  override fun bind(holder: ViewHolder) {
    super.bind(holder)
    holder.tabsDescription.text = holder.tabsDescription.context.resources.getQuantityString(
      R.plurals.active_tabs,
      tabs.size,
      tabs.size
    )
    holder.tabsCard.setOnClickListener {
      tabsManager.showTabsActivity()
    }
    if (tabs.isEmpty()) {
      holder.tabsPreviewRecyclerView.gone()
    } else {
      holder.tabsPreviewRecyclerView.show()
      holder.tabsPreviewRecyclerView.withModels {
        tabs.forEach { tab ->
          tab {
            id(tab.hashCode())
            tab(tab)
            tabsManager(tabsManager)
          }
        }
      }
    }
  }

  class ViewHolder : KotlinHolder() {
    override fun bindView(itemView: View) {
      super.bindView(itemView)
      tabsPreviewRecyclerView.apply {
        (itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        layoutManager = LinearLayoutManager(
          containerView.context,
          RecyclerView.HORIZONTAL,
          false
        )
      }
    }
  }
}
