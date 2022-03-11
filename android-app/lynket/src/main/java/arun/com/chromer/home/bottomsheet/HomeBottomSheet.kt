/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.home.bottomsheet

import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import arun.com.chromer.R
import arun.com.chromer.about.AboutAppActivity
import arun.com.chromer.intro.ChromerIntroActivity
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

  companion object {
    // TODO Refactor to items
    private const val INTRO = 1L
    private const val FEEDBACK = 2L
    private const val RATE = 3L
    private const val BETA = 4L
    private const val SHARE = 5L

    // private const val DONATION = 6L
    private const val ABOUT = 7L
  }

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
          withHeaderBackground(R.drawable.chromer_header_small)
          withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
          withDividerBelowHeader(true)
          build()
        })
      addStickyDrawerItems()
      addDrawerItems(
        PrimaryDrawerItem()
          .withName(getString(R.string.intro))
          .withIdentifier(INTRO)
          .withIcon(CommunityMaterial.Icon.cmd_clipboard_text)
          .withSelectable(false),
        PrimaryDrawerItem()
          .withName(getString(R.string.feedback))
          .withIdentifier(FEEDBACK)
          .withIcon(CommunityMaterial.Icon.cmd_message_text)
          .withSelectable(false),
        PrimaryDrawerItem()
          .withName(getString(R.string.rate_play_store))
          .withIdentifier(RATE)
          .withIcon(CommunityMaterial.Icon.cmd_comment_text)
          .withSelectable(false),
        PrimaryDrawerItem()
          .withName(R.string.join_beta)
          .withIdentifier(BETA)
          .withIcon(CommunityMaterial.Icon.cmd_beta)
          .withSelectable(false),
        DividerDrawerItem(),
        PrimaryDrawerItem()
          .withName(getString(R.string.share))
          .withIcon(CommunityMaterial.Icon.cmd_share_variant)
          .withDescription(getString(R.string.help_chromer_grow))
          .withIdentifier(SHARE)
          .withSelectable(false),
        /*PrimaryDrawerItem()
          .withName(getString(R.string.support_development))
          .withDescription(R.string.consider_donation)
          .withIcon(CommunityMaterial.Icon.cmd_heart)
          .withIconColorRes(R.color.accent)
          .withIdentifier(DONATION)
          .withSelectable(false),*/
        PrimaryDrawerItem()
          .withName(getString(R.string.about))
          .withIcon(CommunityMaterial.Icon.cmd_information)
          .withIdentifier(ABOUT)
          .withSelectable(false)
      )
      withOnDrawerItemClickListener { _, _, drawerItem ->
        when (drawerItem.identifier) {
          FEEDBACK -> {
            val emailIntent = Intent(ACTION_SENDTO, Uri.fromParts("mailto", Constants.MAILID, null))
            emailIntent.putExtra(EXTRA_SUBJECT, getString(R.string.app_name))
            startActivity(createChooser(emailIntent, getString(R.string.send_email)))
          }
          BETA -> startActivity(
            Intent(
              ACTION_VIEW,
              "https://play.google.com/apps/testing/arun.com.chromer".toUri()
            )
          )
          RATE -> Utils.openPlayStore(requireActivity(), requireActivity().packageName)
          INTRO -> startActivity(Intent(requireActivity(), ChromerIntroActivity::class.java))
          // DONATION -> startActivity(Intent(requireActivity(), DonateActivity::class.java))
          SHARE -> {
            val shareIntent = Intent(ACTION_SEND)
            shareIntent.putExtra(EXTRA_TEXT, getString(R.string.share_text))
            shareIntent.type = "text/plain"
            startActivity(createChooser(shareIntent, getString(R.string.share_via)))
          }
          ABOUT -> startActivity(Intent(requireActivity(), AboutAppActivity::class.java))
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
