package arun.com.chromer.util.epoxy

import arun.com.chromer.R
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder

@EpoxyModelClass(layout = R.layout.layout_progress_indeterminate)
abstract class IndeterminateProgressModel : KotlinEpoxyModelWithHolder<IndeterminateProgressModel.ViewHolder>() {
    class ViewHolder : KotlinHolder()
}