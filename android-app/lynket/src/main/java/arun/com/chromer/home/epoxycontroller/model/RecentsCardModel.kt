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

import android.content.Intent
import android.view.View
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.history.HistoryActivity
import arun.com.chromer.shared.epxoy.model.websiteLayout
import arun.com.chromer.tabs.TabsManager
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.layout_feed_recents_list.*

@EpoxyModelClass(layout = R.layout.layout_feed_recents_list)
abstract class RecentsCardModel : KotlinEpoxyModelWithHolder<RecentsCardModel.ViewHolder>() {
  @EpoxyAttribute
  lateinit var websites: List<Website>

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  lateinit var tabsManager: TabsManager

  private val historyClickListener = View.OnClickListener { view ->
    view.context.startActivity(Intent(view.context, HistoryActivity::class.java))
  }

  override fun bind(holder: ViewHolder) {
    holder.apply {
      recentsHeaderIcon.setImageDrawable(
        IconicsDrawable(holder.containerView.context)
          .icon(CommunityMaterial.Icon.cmd_history)
          .colorRes(R.color.accent)
          .sizeDp(24)
      )
      recentsEpoxyGrid.withModels {
        websites.forEach { website ->
          websiteLayout {
            id(website.hashCode())
            website(website)
            tabsManager(tabsManager)
          }
        }
      }
      historyButton.setOnClickListener(historyClickListener)
    }
  }

  class ViewHolder : KotlinHolder()
}
