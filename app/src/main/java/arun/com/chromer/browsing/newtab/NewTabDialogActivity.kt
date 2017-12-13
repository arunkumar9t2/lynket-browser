/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.browsing.newtab

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import arun.com.chromer.R
import arun.com.chromer.browsing.browserintercept.BrowserInterceptActivity
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.search.view.MaterialSearchView
import arun.com.chromer.shared.base.activity.BaseActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import rx.subscriptions.CompositeSubscription

/**
 * Simple dialog activity to launch new url in a popup. Shows a search view.
 */
class NewTabDialogActivity : BaseActivity() {
    private var newTabDialog: NewTabDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newTabDialog = NewTabDialog(this).show()
    }

    override fun getLayoutRes(): Int {
        return 0
    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        newTabDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        newTabDialog?.onActivityResult(requestCode, resultCode, data)
    }

    class NewTabDialog(
            private var activity: Activity?
    ) : DialogInterface.OnDismissListener {
        val subs = CompositeSubscription()
        private lateinit var unbinder: Unbinder
        private var dialog: MaterialDialog? = null

        @BindView(R.id.material_search_view)
        @JvmField
        var materialSearchView: MaterialSearchView? = null

        fun show(): NewTabDialog? {
            dialog = MaterialDialog.Builder(activity!!)
                    .title(R.string.new_tab)
                    .backgroundColorRes(R.color.card_background_light)
                    .icon(IconicsDrawable(activity)
                            .icon(CommunityMaterial.Icon.cmd_plus)
                            .colorRes(R.color.primary)
                            .sizeDp(24))
                    .customView(R.layout.activity_new_tab, false)
                    .dismissListener(this)
                    .show()

            unbinder = ButterKnife.bind(this, dialog!!.customView!!)

            materialSearchView?.apply {
                subs.add(searchPerforms().subscribe { url ->
                    postDelayed({ launchUrl(url) }, 150)
                })
                subs.add(voiceSearchFailed()
                        .subscribe {
                            Toast.makeText(activity, R.string.no_voice_rec_apps, Toast.LENGTH_SHORT).show()
                        })
            }
            return this
        }


        private fun launchUrl(url: String?) {
            activity?.startActivity(Intent(activity, BrowserInterceptActivity::class.java)
                    .apply {
                        data = Uri.parse(url)
                    })
            dialog?.dismiss()
        }

        fun dismiss() {
            dialog?.dismiss()
        }

        override fun onDismiss(dialogInterface: DialogInterface?) {
            subs.clear()
            activity?.finish()
            activity = null
            unbinder.unbind()
            dialog = null
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            materialSearchView?.onActivityResult(requestCode, resultCode, data)
        }
    }
}
