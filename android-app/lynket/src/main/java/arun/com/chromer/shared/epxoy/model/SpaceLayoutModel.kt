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

package arun.com.chromer.shared.epxoy.model

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import arun.com.chromer.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder

@EpoxyModelClass(layout = R.layout.widget_space_layout)
abstract class SpaceLayoutModel : KotlinEpoxyModelWithHolder<SpaceLayoutModel.ViewHolder>() {
  class ViewHolder : KotlinHolder()

  @EpoxyAttribute
  var spaceHeight: Int = 0

  @EpoxyAttribute
  var spaceWidth: Int = 0

  override fun bind(holder: ViewHolder) {
    super.bind(holder)
    holder.containerView.updateLayoutParams<ViewGroup.LayoutParams> {
      height = spaceHeight
      width = spaceWidth
    }
  }
}
