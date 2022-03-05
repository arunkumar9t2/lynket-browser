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

package arun.com.chromer.search.provider

import android.view.View
import arun.com.chromer.R
import arun.com.chromer.util.glide.GlideApp
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.widget_search_provider_item.*

@EpoxyModelClass(layout = R.layout.widget_search_provider_item)
abstract class SearchProviderLayoutModel :
  KotlinEpoxyModelWithHolder<SearchProviderLayoutModel.ViewHolder>() {
  class ViewHolder : KotlinHolder()

  @EpoxyAttribute
  lateinit var searchProvider: SearchProvider

  @EpoxyAttribute(DoNotHash)
  lateinit var onClick: View.OnClickListener

  override fun bind(holder: ViewHolder) {
    super.bind(holder)
    holder.apply {
      GlideApp.with(searchProviderIcon).load(searchProvider.iconUri).into(searchProviderIcon)
      searchProviderName.text = searchProvider.name
    }
    holder.containerView.setOnClickListener(onClick)
  }
}
