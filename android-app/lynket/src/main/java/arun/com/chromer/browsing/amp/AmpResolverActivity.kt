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

package arun.com.chromer.browsing.amp

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.tabs.TabsManager
import com.afollestad.materialdialogs.MaterialDialog
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class AmpResolverActivity : BrowsingActivity() {
  private var ampResolverDialog: AmpResolverDialog? = null

  @Inject
  lateinit var tabsManager: TabsManager

  override val layoutRes: Int get() = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ampResolverDialog = AmpResolverDialog(this).show()
  }

  override fun inject(activityComponent: ActivityComponent) {
    activityComponent.inject(this)
  }

  override fun onWebsiteLoaded(website: Website) {
    ampResolverDialog?.loadedDetails()
  }

  private fun launchUrl() {
    if (website!!.hasAmp()) {
      tabsManager.openBrowsingTab(this, Website.Ampify(website!!), fromNewTab = false)
    } else {
      tabsManager.openUrl(this, Website.Ampify(website!!), fromAmp = true)
    }
    ampResolverDialog?.dismiss()
  }


  override fun onDestroy() {
    super.onDestroy()
    ampResolverDialog?.dismiss()
  }

  inner class AmpResolverDialog(
    private var activity: Activity?
  ) : DialogInterface.OnDismissListener {
    val subs = CompositeSubscription()
    private var dialog: MaterialDialog? = null

    fun show(): AmpResolverDialog {
      dialog = MaterialDialog.Builder(activity!!)
        .title(R.string.grabbing_amp_link)
        .progress(true, Integer.MAX_VALUE)
        .content(R.string.loading)
        .dismissListener(this)
        .positiveText(R.string.skip)
        .onPositive { _, _ -> launchUrl() }
        .show()
      return this
    }

    fun loadedDetails() {
      if (!TextUtils.isEmpty(website?.ampUrl)) {
        dialog?.setContent(R.string.link_found)
      } else {
        dialog?.setContent(R.string.link_not_found)
      }
      Handler().postDelayed({
        launchUrl()
      }, 200)
    }

    fun dismiss() {
      dialog?.dismiss()
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
      subs.clear()
      activity?.finish()
      activity = null
      dialog = null
    }
  }
}
