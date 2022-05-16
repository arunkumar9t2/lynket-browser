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

package arun.com.chromer.shortcuts

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.browsing.browserintercept.BrowserInterceptActivity
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.extenstions.toBitmap
import arun.com.chromer.extenstions.watch
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.internal.MDButton
import com.bumptech.glide.request.target.ImageViewTarget
import com.google.android.material.textfield.TextInputLayout
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import rx.subscriptions.CompositeSubscription


class HomeScreenShortcutCreatorActivity : BrowsingActivity() {

  override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)
  override fun onWebsiteLoaded(website: Website) {
  }

  override val layoutRes: Int get() = 0

  private var shortcutDialog: MaterialDialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    shortcutDialog = AddShortcutDialog(this, browsingViewModel.websiteLiveData).show()
  }

  override fun onDestroy() {
    super.onDestroy()
    shortcutDialog?.dismiss()
  }

  class AddShortcutDialog(
    private var activity: Activity?,
    private val websiteLiveData: MutableLiveData<Result<Website>>
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

    private lateinit var website: Website

    fun show(): MaterialDialog? {
      dialog = MaterialDialog.Builder(activity!!)
        .title(R.string.create_shorcut)
        .customView(R.layout.dialog_create_shorcut_layout, false)
        .dismissListener(this)
        .positiveText(R.string.create)
        .autoDismiss(true)
        .onPositive { _, _ ->
          if (ShortcutManagerCompat.isRequestPinShortcutSupported(activity!!)) {
            ShortcutManagerCompat.requestPinShortcut(
              activity!!,
              ShortcutInfoCompat.Builder(activity!!, website.url)
                .setIcon(IconCompat.createWithBitmap(iconView!!.drawable!!.toBitmap()))
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

      websiteLiveData.watch(activity as LifecycleOwner) {
        when (it) {
          is Result.Loading<Website> -> {
            progressBar?.show()
            iconView?.gone()
            shortcutName?.setText(R.string.loading)
            positiveButton?.isEnabled = false
          }
          is Result.Success<Website> -> {
            website = it.data!!
            shortcutName?.setText(website.safeLabel())
            GlideApp.with(activity!!)
              .load(it.data)
              .error(
                GlideApp.with(activity!!).load(ApplicationIcon.createUri(activity!!.packageName))
              )
              .into(object : ImageViewTarget<Drawable>(iconView) {
                override fun setResource(resource: Drawable?) {
                  iconView?.setImageDrawable(resource)
                  loadAttemptFinished()
                }
              })
          }
          else -> {
          }
        }
      }
      return dialog
    }

    private fun loadAttemptFinished() {
      progressBar?.gone()
      iconView?.show()
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
      iconView?.postDelayed({ positiveButton?.isEnabled = iconView?.drawable != null }, 200)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
  }
}

