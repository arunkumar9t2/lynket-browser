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

package arun.com.chromer.util.recyclerview

import androidx.recyclerview.widget.RecyclerView

fun <T : RecyclerView.ViewHolder> RecyclerView.Adapter<T>.onChanges(action: () -> Unit) {
  registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
    override fun onChanged() = action()
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = action()
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = action()
    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = action()
    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = action()
  })
}
