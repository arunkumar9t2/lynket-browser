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
import android.net.Uri
import arun.com.chromer.R
import arun.com.chromer.browsing.providerselection.ProviderSelectionActivity
import arun.com.chromer.extenstions.StringResource
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.resolveStringResource
import arun.com.chromer.util.HtmlCompat
import arun.com.chromer.util.glide.GlideApp
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.layout_provider_info_card.*

data class CustomTabProviderInfo(
  val iconUri: Uri,
  val providerDescription: StringResource,
  val providerReason: StringResource,
  val allowChange: Boolean = true
)

@EpoxyModelClass(layout = R.layout.layout_provider_info_card)
abstract class ProviderInfoModel : KotlinEpoxyModelWithHolder<ProviderInfoModel.ViewHolder>() {
  @EpoxyAttribute
  lateinit var providerInfo: CustomTabProviderInfo

  override fun bind(holder: ViewHolder) {
    GlideApp.with(holder.providerIcon)
      .load(providerInfo.iconUri)
      .error(
        IconicsDrawable(holder.providerIcon.context)
          .icon(CommunityMaterial.Icon.cmd_web)
          .colorRes(R.color.primary)
          .sizeDp(36)
      )
      .into(holder.providerIcon)
    holder.providerDescription.run {
      text = HtmlCompat.fromHtml(context.resolveStringResource(providerInfo.providerDescription))
    }
    if (providerInfo.providerReason.resource != 0) {
      holder.providerReason.run {
        text = context.resolveStringResource(providerInfo.providerReason)
      }
    } else {
      holder.providerReason.gone()
    }
    holder.providerChangeButton.run {
      gone(!providerInfo.allowChange)
      setOnClickListener {
        context.startActivity(Intent(context, ProviderSelectionActivity::class.java))
      }
    }
  }

  class ViewHolder : KotlinHolder()
}
