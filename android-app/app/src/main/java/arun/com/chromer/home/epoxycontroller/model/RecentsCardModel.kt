package arun.com.chromer.home.epoxycontroller.model

import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.glide.GlideApp
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.activity_main_recents_item_template.*
import kotlinx.android.synthetic.main.layout_feed_recents_list.*

@EpoxyModelClass(layout = R.layout.activity_main_recents_item_template)
abstract class RecentItem : KotlinEpoxyModelWithHolder<RecentItem.ViewHolder>() {
    class ViewHolder : KotlinHolder()

    @EpoxyAttribute
    lateinit var website: Website
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var tabsManager: TabsManager

    override fun bind(holder: ViewHolder) {
        holder.apply {
            label.text = website.safeLabel()
            containerView.setOnClickListener {
                tabsManager.openUrl(containerView.context, website)
            }
            GlideApp.with(containerView.context)
                    .load(website)
                    .into(icon)
        }
    }
}

@EpoxyModelClass(layout = R.layout.layout_feed_recents_list)
abstract class RecentsCardModel : KotlinEpoxyModelWithHolder<RecentsCardModel.ViewHolder>() {
    @EpoxyAttribute
    lateinit var websites: List<Website>
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var tabsManager: TabsManager

    override fun bind(holder: ViewHolder) {
        holder.apply {
            recentsHeaderIcon.setImageDrawable(
                    IconicsDrawable(holder.containerView.context)
                            .icon(CommunityMaterial.Icon.cmd_history)
                            .colorRes(R.color.accent)
                            .sizeDp(24)
            )
            recentsEpoxyGrid.withModels {
                websites.forEach { website ->
                    recentItem {
                        id(website.hashCode())
                        website(website)
                        tabsManager(tabsManager)
                    }
                }
            }
        }
    }

    class ViewHolder : KotlinHolder()
}