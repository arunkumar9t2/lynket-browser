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
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import arun.com.chromer.R
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.model.WebSite
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.visible
import arun.com.chromer.shared.common.BaseActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.internal.MDButton
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
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
        internal lateinit var iconView: ImageView
        @BindView(R.id.shortcut_name)
        internal lateinit var shortcutName: EditText
        @BindView(R.id.shortcut_name_wrapper)
        internal lateinit var shortcutNameWrapper: TextInputLayout
        @BindView(R.id.extract_progress)
        internal lateinit var progressBar: MaterialProgressBar

        val subs = CompositeSubscription()

        fun show(): MaterialDialog? {
            dialog = MaterialDialog.Builder(activity!!)
                    .title(R.string.create_shorcut)
                    .customView(R.layout.dialog_create_shorcut_layout, false)
                    .dismissListener(this)
                    .positiveText(R.string.create)
                    .autoDismiss(false)
                    .onPositive { _, _ ->
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                            }
                            else -> {
                                // AddToHomeScreenIntentService.createShortcut(activity!!, iconUri.toString(), icon.packageName, shortcutName.text.toString())
                                dialog?.dismiss()
                            }
                        }
                    }
                    .show()
            val dialogView = dialog!!.customView
            unbinder = ButterKnife.bind(this, dialogView!!)
            shortcutName.addTextChangedListener(this)


            positiveButton?.isEnabled = false

            subs.add(webSiteObservable.subscribe {
                when (it) {
                    is Result.Loading<WebSite> -> {
                        progressBar.visible()
                        iconView.gone()
                        shortcutName.setText(R.string.loading)
                        positiveButton?.isEnabled = false
                    }
                    is Result.Success<WebSite> -> {
                        shortcutName.setText(it.data.safeLabel())
                        Glide.with(activity)
                                .load(it.data.faviconUrl)
                                .listener(object : RequestListener<String, GlideDrawable> {
                                    override fun onException(e: java.lang.Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                                        loadAttemptFinished()
                                        return false
                                    }

                                    override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                        loadAttemptFinished()
                                        return false
                                    }
                                })
                                .into(iconView)
                    }
                    else -> {
                        loadAttemptFinished()
                    }
                }
            })

            return dialog
        }

        private fun loadAttemptFinished() {
            progressBar.gone()
            iconView.visible()
            positiveButton?.isEnabled = true
        }

        override fun onDismiss(dialogInterface: DialogInterface) {
            activity?.finish()
            activity = null
            subs.clear()
            unbinder.unbind()
            dialog = null
        }

        private val positiveButton: MDButton?
            get() {
                return dialog!!.getActionButton(DialogAction.POSITIVE)
            }

        override fun afterTextChanged(editable: Editable?) {
            if (editable.toString().trim { it <= ' ' }.isEmpty()) {
                shortcutNameWrapper.isErrorEnabled = true
                shortcutNameWrapper.error = activity!!.getString(R.string.name_cannot_be_empty)
                positiveButton?.isEnabled = false
            } else {
                shortcutNameWrapper.isErrorEnabled = false
                positiveButton?.isEnabled = true
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
}

