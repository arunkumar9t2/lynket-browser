package arun.com.chromer.home.bottomsheet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arun.com.chromer.R
import arun.com.chromer.about.AboutAppActivity
import arun.com.chromer.intro.ChromerIntroActivity
import arun.com.chromer.payments.DonateActivity
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.Utils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.layout_home_bottom_sheet.*

class HomeBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.layout_home_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val drawer = DrawerBuilder().run {
            withActivity(requireActivity())
            withSavedInstance(savedInstanceState)
            withAccountHeader(
                    AccountHeaderBuilder().run {
                        withActivity(requireActivity())
                        withHeaderBackground(R.drawable.lynket_drawer_image)
                        withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                        withDividerBelowHeader(true)
                        build()
                    })
            addStickyDrawerItems()
            addDrawerItems(
                    PrimaryDrawerItem()
                            .withName(getString(R.string.intro))
                            .withIdentifier(4)
                            .withIcon(CommunityMaterial.Icon.cmd_clipboard_text)
                            .withSelectable(false),
                    PrimaryDrawerItem()
                            .withName(getString(R.string.feedback))
                            .withIdentifier(2)
                            .withIcon(CommunityMaterial.Icon.cmd_message_text)
                            .withSelectable(false),
                    PrimaryDrawerItem()
                            .withName(getString(R.string.rate_play_store))
                            .withIdentifier(3)
                            .withIcon(CommunityMaterial.Icon.cmd_comment_text)
                            .withSelectable(false),
                    PrimaryDrawerItem()
                            .withName(R.string.join_beta)
                            .withIdentifier(9)
                            .withIcon(CommunityMaterial.Icon.cmd_beta)
                            .withSelectable(false),
                    DividerDrawerItem(),
                    PrimaryDrawerItem()
                            .withName(getString(R.string.share))
                            .withIcon(CommunityMaterial.Icon.cmd_share_variant)
                            .withDescription(getString(R.string.help_chromer_grow))
                            .withIdentifier(7)
                            .withSelectable(false),
                    PrimaryDrawerItem()
                            .withName(getString(R.string.support_development))
                            .withDescription(R.string.consider_donation)
                            .withIcon(CommunityMaterial.Icon.cmd_heart)
                            .withIconColorRes(R.color.accent)
                            .withIdentifier(6)
                            .withSelectable(false),
                    PrimaryDrawerItem()
                            .withName(getString(R.string.about))
                            .withIcon(CommunityMaterial.Icon.cmd_information)
                            .withIdentifier(8)
                            .withSelectable(false)
            )
            withOnDrawerItemClickListener { _, _, drawerItem ->
                when (drawerItem.identifier.toInt()) {
                    2 -> {
                        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", Constants.MAILID, null))
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
                    }
                    3 -> Utils.openPlayStore(requireActivity(), requireActivity().packageName)
                    4 -> startActivity(Intent(requireActivity(), ChromerIntroActivity::class.java))
                    6 -> startActivity(Intent(requireActivity(), DonateActivity::class.java))
                    7 -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
                        shareIntent.type = "text/plain"
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
                    }
                    8 -> startActivity(Intent(requireActivity(), AboutAppActivity::class.java))
                }
                false
            }
            withDelayDrawerClickEvent(200)
            buildView()
        }.apply {
            setSelection(-1)
        }
        homeBottomSheet.addView(drawer.slider)
    }
}