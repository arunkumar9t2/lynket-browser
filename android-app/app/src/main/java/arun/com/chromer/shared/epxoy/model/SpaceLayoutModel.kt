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