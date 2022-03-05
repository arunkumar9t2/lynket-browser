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

package arun.com.chromer.util

import androidx.recyclerview.widget.RecyclerView

open class SimpleAdapterDataSetObserver(private val onAnyChanges: () -> Unit) :
  RecyclerView.AdapterDataObserver() {
  override fun onChanged() = onAnyChanges()

  override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = onAnyChanges()

  override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = onAnyChanges()

  override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = onAnyChanges()

  override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = onAnyChanges()
}
