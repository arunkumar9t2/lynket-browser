package arun.com.chromer.shared.epxoy.model

import androidx.core.text.toSpannable
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.makeMatchingBold
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.widget_website_grid_item.*

@EpoxyModelClass(layout = R.layout.widget_website_grid_item)
abstract class WebsiteLayoutModel : KotlinEpoxyModelWithHolder<WebsiteLayoutModel.ViewHolder>() {
  class ViewHolder : KotlinHolder()

  @EpoxyAttribute
  lateinit var website: Website

  @EpoxyAttribute(DoNotHash)
  lateinit var tabsManager: TabsManager

  @EpoxyAttribute
  var query: String = ""

  override fun bind(holder: ViewHolder) {
    holder.apply {
      label.text = website.safeLabel().toSpannable().makeMatchingBold(query)
      containerView.setOnClickListener {
        tabsManager.openUrl(containerView.context, website)
      }
      GlideApp.with(containerView.context)
          .load(website)
          .into(icon)
    }
  }
}