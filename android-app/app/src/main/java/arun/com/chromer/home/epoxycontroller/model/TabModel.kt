package arun.com.chromer.home.epoxycontroller.model

import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.glide.GlideApp
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.widget_tab_model_preview.*

@EpoxyModelClass(layout = R.layout.widget_tab_model_preview)
abstract class TabModel : KotlinEpoxyModelWithHolder<TabModel.ViewHolder>() {
    @EpoxyAttribute
    lateinit var tab: TabsManager.Tab
    @EpoxyAttribute(DoNotHash)
    lateinit var tabsManager: TabsManager

    override fun bind(holder: ViewHolder) {
        super.bind(holder)
        GlideApp.with(holder.containerView.context)
                .load(tab.website ?: Website(tab.url))
                .circleCrop()
                .into(holder.icon)
        holder.containerView.setOnClickListener {
            tabsManager.reOrderTabByUrl(
                    holder.containerView.context,
                    Website(tab.url),
                    listOf(tab.getTargetActivityName())
            )
        }
    }

    class ViewHolder : KotlinHolder()
}