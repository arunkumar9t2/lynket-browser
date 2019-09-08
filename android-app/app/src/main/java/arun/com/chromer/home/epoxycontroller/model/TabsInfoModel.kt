package arun.com.chromer.home.epoxycontroller.model

import arun.com.chromer.R
import arun.com.chromer.tabs.TabsManager
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.layout_tabs_info_card.*

@EpoxyModelClass(layout = R.layout.layout_tabs_info_card)
abstract class TabsInfoModel : KotlinEpoxyModelWithHolder<TabsInfoModel.ViewHolder>() {
    @EpoxyAttribute
    lateinit var tabs: List<TabsManager.Tab>
    @EpoxyAttribute(DoNotHash)
    lateinit var tabsManager: TabsManager

    private var revealed = false

    override fun bind(holder: ViewHolder) {
        super.bind(holder)
        holder.tabsDescription.text = holder.tabsDescription.context.resources.getQuantityString(
                R.plurals.active_tabs,
                tabs.size,
                tabs.size
        )
        holder.containerView.setOnClickListener {
            tabsManager.showTabsActivity()
        }
        /*holder.containerView.post {
            if (!revealed) {
                holder.tabsRevealView.run {
                    post {
                        circularRevealWithSelfCenter {
                            postDelayed(50) {
                                (holder.containerView as ViewGroup).prepareAutoTransition()
                                gone()
                            }
                        }
                    }
                }
                revealed = true
            }
        }*/
    }

    class ViewHolder : KotlinHolder()
}