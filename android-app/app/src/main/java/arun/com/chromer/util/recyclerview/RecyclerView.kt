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