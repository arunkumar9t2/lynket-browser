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

package arun.com.chromer.perapp

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.data.common.App
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.RequestManager
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import dev.arunkumar.android.dagger.activity.PerActivity
import rx.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by Arun on 24/01/2016.
 */
@PerActivity
class PerAppListAdapter @Inject
internal constructor(
  private val activity: Activity,
  private val glideRequests: RequestManager
) : RecyclerView.Adapter<PerAppListAdapter.BlackListItemViewHolder>() {
  private val apps = ArrayList<App>()

  private val iconSizeDp = 24

  val incognitoSelections: PublishSubject<Pair<String, Boolean>> =
    PublishSubject.create()
  val blacklistSelections: PublishSubject<Pair<String, Boolean>> =
    PublishSubject.create()

  private val blacklistSelected: IconicsDrawable by lazy {
    IconicsDrawable(activity).apply {
      icon(CommunityMaterial.Icon.cmd_earth)
      colorRes(R.color.accent)
      sizeDp(iconSizeDp)
    }
  }

  private val blacklistUnSelected: IconicsDrawable by lazy {
    IconicsDrawable(activity).apply {
      icon(CommunityMaterial.Icon.cmd_earth)
      colorRes(R.color.material_dark_light)
      sizeDp(iconSizeDp)
    }
  }

  private val incognitoSelected: IconicsDrawable by lazy {
    IconicsDrawable(activity).apply {
      icon(CommunityMaterial.Icon.cmd_incognito)
      colorRes(R.color.accent)
      sizeDp(iconSizeDp)
    }
  }

  private val incognitoUnSelected: IconicsDrawable by lazy {
    IconicsDrawable(activity).apply {
      icon(CommunityMaterial.Icon.cmd_incognito)
      colorRes(R.color.material_dark_light)
      sizeDp(iconSizeDp)
    }
  }

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlackListItemViewHolder {
    return BlackListItemViewHolder(
      LayoutInflater.from(parent.context)
        .inflate(R.layout.activity_per_apps_list_item_template, parent, false)
    )
  }

  override fun onBindViewHolder(holder: BlackListItemViewHolder, position: Int) {
    holder.bind(apps[position])
  }

  override fun onViewRecycled(holder: BlackListItemViewHolder) {
    super.onViewRecycled(holder)
    glideRequests.clear(holder.appIcon!!)
  }

  override fun getItemCount(): Int {
    return apps.size
  }

  override fun getItemId(position: Int): Long {
    return apps[position].hashCode().toLong()
  }

  fun setApps(apps: List<App>) {
    this.apps.clear()
    this.apps.addAll(apps)
    notifyDataSetChanged()
  }

  fun setApp(index: Int, app: App) {
    this.apps[index] = app
    notifyItemChanged(index)
  }

  inner class BlackListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    @BindView(R.id.app_list_icon)
    @JvmField
    var appIcon: ImageView? = null

    @BindView(R.id.app_list_name)
    @JvmField
    var appName: TextView? = null

    @BindView(R.id.app_list_package)
    @JvmField
    var appPackage: TextView? = null

    @BindView(R.id.incognitoIcon)
    @JvmField
    var incognitoIcon: ImageView? = null

    @BindView(R.id.blacklistIcon)
    @JvmField
    var blacklistIcon: ImageView? = null

    init {
      ButterKnife.bind(this, view)
    }

    fun bind(app: App) {
      appName!!.text = app.appName
      appPackage!!.text = app.packageName
      glideRequests.load(ApplicationIcon.createUri(app.packageName)).into(appIcon!!)

      blacklistIcon!!.apply {
        setImageDrawable(if (app.blackListed) blacklistSelected else blacklistUnSelected)
        setOnClickListener {
          if (adapterPosition != RecyclerView.NO_POSITION) {
            val currentApp = apps[adapterPosition]
            currentApp.blackListed = !currentApp.blackListed
            blacklistSelections.onNext(Pair(currentApp.packageName, currentApp.blackListed))
          }
        }
      }

      incognitoIcon!!.apply {
        setImageDrawable(if (app.incognito) incognitoSelected else incognitoUnSelected)
        setOnClickListener {
          if (adapterPosition != RecyclerView.NO_POSITION) {
            val currentApp = apps[adapterPosition]
            currentApp.incognito = !currentApp.incognito
            incognitoSelections.onNext(Pair(currentApp.packageName, currentApp.incognito))
          }
        }
      }
    }
  }
}
