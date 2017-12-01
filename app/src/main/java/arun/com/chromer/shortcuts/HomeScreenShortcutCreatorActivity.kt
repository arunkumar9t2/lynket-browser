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

package arun.com.chromer.shortcuts

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import arun.com.chromer.R
import arun.com.chromer.activities.browserintercept.BrowserInterceptActivity
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.model.WebSite
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.toBitmap
import arun.com.chromer.extenstions.visible
import arun.com.chromer.glide.GlideApp
import arun.com.chromer.glide.appicon.ApplicationIcon
import arun.com.chromer.shared.common.BaseActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.internal.MDButton
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import rx.Observable
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject


class HomeScreenShortcutCreatorActivity : BaseActivity() {
    override fun getLayoutRes(): Int = 0
    override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var shortcutViewModel: HomeScreenShortcutViewModel
    private var shortcutDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.dataString != null) {
            shortcutViewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeScreenShortcutViewModel::class.java)

            val webSiteObservable = shortcutViewModel.loadWebSiteDetails(intent.dataString)
            shortcutDialog = AddShortcutDialog(this, webSiteObservable).show()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shortcutDialog?.dismiss()
    }

    class AddShortcutDialog(
            private var activity: Activity?,
            private val webSiteObservable: Observable<Result<WebSite>>
    ) : DialogInterface.OnDismissListener, TextWatcher {
        private lateinit var unbinder: Unbinder
        private var dialog: MaterialDialog? = null

        @BindView(R.id.icon_view)
        @JvmField
        var iconView: ImageView? = null
        @BindView(R.id.shortcut_name)
        @JvmField
        var shortcutName: EditText? = null
        @BindView(R.id.shortcut_name_wrapper)
        @JvmField
        var shortcutNameWrapper: TextInputLayout? = null
        @BindView(R.id.extract_progress)
        @JvmField
        var progressBar: MaterialProgressBar? = null

        val subs = CompositeSubscription()

        private lateinit var website: WebSite

        fun show(): MaterialDialog? {
            dialog = MaterialDialog.Builder(activity!!)
                    .title(R.string.create_shorcut)
                    .customView(R.layout.dialog_create_shorcut_layout, false)
                    .dismissListener(this)
                    .positiveText(R.string.create)
                    .autoDismiss(false)
                    .onPositive { _, _ ->
                        if (ShortcutManagerCompat.isRequestPinShortcutSupported(activity!!)) {
                            ShortcutManagerCompat.requestPinShortcut(
                                    activity!!,
                                    ShortcutInfoCompat.Builder(activity!!, website.url)
                                            .setIcon(IconCompat.createWithBitmap(iconView?.drawable?.toBitmap()))
                                            .setIntent(Intent(activity, BrowserInterceptActivity::class.java).apply {
                                                action = Intent.ACTION_VIEW
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                data = Uri.parse(website.preferredUrl())
                                            })
                                            .setLongLabel(shortcutName?.text.toString())
                                            .setShortLabel(shortcutName?.text.toString())
                                            .build(),
                                    null
                            )
                        }
                    }
                    .show()
            val dialogView = dialog!!.customView
            unbinder = ButterKnife.bind(this, dialogView!!)
            shortcutName?.addTextChangedListener(this)
            positiveButton?.isEnabled = false

            subs.add(webSiteObservable.subscribe {
                when (it) {
                    is Result.Loading<WebSite> -> {
                        progressBar?.visible()
                        iconView?.gone()
                        shortcutName?.setText(R.string.loading)
                        positiveButton?.isEnabled = false
                    }
                    is Result.Success<WebSite> -> {
                        website = it.data
                        shortcutName?.setText(website.safeLabel())
                        GlideApp.with(activity)
                                .load(it.data)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                                        GlideApp.with(activity)
                                                .load(ApplicationIcon.createUri(activity!!.packageName))
                                                .into(object : ImageViewTarget<Drawable>(iconView) {
                                                    override fun setResource(resource: Drawable?) {
                                                        iconView?.setImageDrawable(resource)
                                                        loadAttemptFinished()
                                                    }
                                                })
                                        return false
                                    }

                                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                                        return false
                                    }
                                })
                                .into(object : ImageViewTarget<Drawable>(iconView) {
                                    override fun setResource(resource: Drawable?) {
                                        iconView?.setImageDrawable(resource)
                                        loadAttemptFinished()
                                    }
                                })
                    }
                }
            })

            return dialog
        }

        private fun loadAttemptFinished() {
            progressBar?.gone()
            iconView?.visible()
            enablePositiveButtonIfImageLoaded()
        }

        override fun onDismiss(dialogInterface: DialogInterface) {
            subs.clear()
            activity?.finish()
            activity = null
            unbinder.unbind()
            dialog = null
        }

        private val positiveButton: MDButton?
            get() {
                return dialog?.getActionButton(DialogAction.POSITIVE)
            }

        override fun afterTextChanged(editable: Editable?) {
            if (editable.toString().trim { it <= ' ' }.isEmpty()) {
                shortcutNameWrapper?.isErrorEnabled = true
                shortcutNameWrapper?.error = activity!!.getString(R.string.name_cannot_be_empty)
                positiveButton?.isEnabled = false
            } else {
                shortcutNameWrapper?.isErrorEnabled = false
                enablePositiveButtonIfImageLoaded()
            }
        }

        private fun enablePositiveButtonIfImageLoaded() {
            positiveButton?.isEnabled = iconView?.drawable != null
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
}

