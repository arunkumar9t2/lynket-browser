package arun.com.chromer.home.epoxycontroller.model

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
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

  override fun bind(holder: ViewHolder) {
    super.bind(holder)
    holder.tabsDescription.text = holder.tabsDescription.context.resources.getQuantityString(
        R.plurals.active_tabs,
        tabs.size,
        tabs.size
    )
    holder.tabsCard.setOnClickListener {
      tabsManager.showTabsActivity()
    }
    if (tabs.isEmpty()) {
      holder.tabsPreviewRecyclerView.gone()
    } else {
      holder.tabsPreviewRecyclerView.show()
      holder.tabsPreviewRecyclerView.withModels {
        tabs.forEach { tab ->
          tab {
            id(tab.hashCode())
            tab(tab)
            tabsManager(tabsManager)
          }
        }
      }
    }
  }

  class ViewHolder : KotlinHolder() {
    override fun bindView(itemView: View) {
      super.bindView(itemView)
      tabsPreviewRecyclerView.apply {
        (itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        layoutManager = LinearLayoutManager(
            containerView.context,
            RecyclerView.HORIZONTAL,
            false
        )
      }
    }
  }
}