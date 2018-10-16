package arun.com.chromer.util

import android.support.v7.widget.RecyclerView

open class SimpleAdapterDataSetObserver(private val onAnyChanges: () -> Unit) : RecyclerView.AdapterDataObserver() {
    override fun onChanged() = onAnyChanges()

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = onAnyChanges()

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = onAnyChanges()

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = onAnyChanges()

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = onAnyChanges()
}