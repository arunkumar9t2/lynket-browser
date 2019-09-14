package arun.com.chromer.home.epoxycontroller.model

import android.content.Intent
import android.net.Uri
import arun.com.chromer.R
import arun.com.chromer.browsing.providerselection.ProviderSelectionActivity
import arun.com.chromer.extenstions.StringResource
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.resolveStringResource
import arun.com.chromer.util.HtmlCompat
import arun.com.chromer.util.glide.GlideApp
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.layout_provider_info_card.*

data class CustomTabProviderInfo(
        val iconUri: Uri,
        val providerDescription: StringResource,
        val providerReason: StringResource,
        val allowChange: Boolean = true
)

@EpoxyModelClass(layout = R.layout.layout_provider_info_card)
abstract class ProviderInfoModel : KotlinEpoxyModelWithHolder<ProviderInfoModel.ViewHolder>() {
    @EpoxyAttribute
    lateinit var providerInfo: CustomTabProviderInfo

    override fun bind(holder: ViewHolder) {
        GlideApp.with(holder.providerIcon)
                .load(providerInfo.iconUri)
                .error(IconicsDrawable(holder.providerIcon.context)
                        .icon(CommunityMaterial.Icon.cmd_web)
                        .colorRes(R.color.primary)
                        .sizeDp(36))
                .into(holder.providerIcon)
        holder.providerDescription.run {
            text = HtmlCompat.fromHtml(context.resolveStringResource(providerInfo.providerDescription))
        }
        if (providerInfo.providerReason.resource != 0) {
            holder.providerReason.run {
                text = context.resolveStringResource(providerInfo.providerReason)
            }
        } else {
            holder.providerReason.gone()
        }
        holder.providerChangeButton.run {
            gone(!providerInfo.allowChange)
            setOnClickListener {
                context.startActivity(Intent(context, ProviderSelectionActivity::class.java))
            }
        }
    }

    class ViewHolder : KotlinHolder()
}