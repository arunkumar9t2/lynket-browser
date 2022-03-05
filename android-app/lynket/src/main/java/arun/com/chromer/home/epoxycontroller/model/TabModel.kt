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

import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.glide.GlideApp
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.widget_tab_model_preview.*

@EpoxyModelClass(layout = R.layout.widget_tab_model_preview)
abstract class TabModel : KotlinEpoxyModelWithHolder<TabModel.ViewHolder>() {
  @EpoxyAttribute
  lateinit var tab: TabsManager.Tab

  @EpoxyAttribute(DoNotHash)
  lateinit var tabsManager: TabsManager

  override fun bind(holder: ViewHolder) {
    super.bind(holder)
    GlideApp.with(holder.containerView.context)
      .load(tab.website ?: Website(tab.url))
      .circleCrop()
      .into(holder.icon)
    holder.containerView.setOnClickListener {
      tabsManager.reOrderTabByUrl(
        holder.containerView.context,
        Website(tab.url),
        listOf(tab.getTargetActivityName())
      )
    }
  }

  class ViewHolder : KotlinHolder()
}
