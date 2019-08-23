package arun.com.chromer.home.epoxycontroller.model

import arun.com.chromer.R
import arun.com.chromer.tabs.TabsManager
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.layout_tabs_info_card.*

@EpoxyModelClass(layout = R.layout.layout_tabs_info_card)
abstract class TabsInfoModel : KotlinEpoxyModelWithHolder<TabsInfoModel.ViewHolder>() {
    @EpoxyAttribute
    lateinit var tabs: List<TabsManager.Tab>
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var tabsManager: TabsManager

    override fun bind(holder: ViewHolder) {
        super.bind(holder)
        holder.tabsDescription.text = holder.tabsDescription.context.getString(R.string.active_tabs, tabs.size)
        holder.tabsButton.setOnClickListener {
            tabsManager.showTabsActivity()
        }
    }

    class ViewHolder : KotlinHolder()
}