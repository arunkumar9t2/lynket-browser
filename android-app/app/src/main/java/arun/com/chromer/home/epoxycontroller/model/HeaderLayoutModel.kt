package arun.com.chromer.home.epoxycontroller.model

import arun.com.chromer.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.layout_feed_header.*

@EpoxyModelClass(layout = R.layout.layout_feed_header)
abstract class HeaderLayoutModel : KotlinEpoxyModelWithHolder<HeaderLayoutModel.ViewHolder>() {
    @EpoxyAttribute
    lateinit var title: String

    override fun bind(holder: ViewHolder) {
        holder.feedHeader.text = title
    }

    class ViewHolder : KotlinHolder()
}