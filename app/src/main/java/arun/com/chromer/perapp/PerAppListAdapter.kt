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

package arun.com.chromer.perapp

import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import arun.com.chromer.R
import arun.com.chromer.data.common.App
import arun.com.chromer.di.scopes.PerActivity
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.RequestManager
import java.util.*
import javax.inject.Inject

/**
 * Created by Arun on 24/01/2016.
 */
@PerActivity
class PerAppListAdapter @Inject
internal constructor(
        private val glideRequests: RequestManager
) : RecyclerView.Adapter<PerAppListAdapter.BlackListItemViewHolder>() {
    private val apps = ArrayList<App>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlackListItemViewHolder {
        return BlackListItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_blacklist_list_item_template, parent, false))
    }

    override fun onBindViewHolder(holder: BlackListItemViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun onViewRecycled(holder: BlackListItemViewHolder?) {
        super.onViewRecycled(holder)
        glideRequests.clear(holder!!.appIcon!!)
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
        @BindView(R.id.app_list_checkbox)
        @JvmField
        var appCheckbox: AppCompatCheckBox? = null
        @BindView(R.id.blacklist_template_root)
        @JvmField
        var blacklistTemplateRoot: LinearLayout? = null

        init {
            ButterKnife.bind(this, view)
            blacklistTemplateRoot?.setOnClickListener { appCheckbox?.performClick() }
        }

        fun bind(app: App) {
            appName!!.text = app.appName
            appPackage!!.text = app.packageName
            glideRequests.load(ApplicationIcon.createUri(app.packageName)).into(appIcon!!)
        }
    }
}