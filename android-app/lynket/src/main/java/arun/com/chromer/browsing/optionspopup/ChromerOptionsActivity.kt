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

package arun.com.chromer.browsing.optionspopup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.browsing.openwith.OpenIntentWithActivity
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.history.HistoryActivity
import arun.com.chromer.settings.SettingsGroupActivity
import arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_ARTICLE
import arun.com.chromer.shared.Constants.EXTRA_KEY_ORIGINAL_URL
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.shortcuts.HomeScreenShortcutCreatorActivity
import arun.com.chromer.tabs.TabsManager
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_more_menu.*
import kotlinx.android.synthetic.main.activity_more_menu_item_template.*
import javax.inject.Inject


class ChromerOptionsActivity : BaseActivity() {

  private var fromArticle: Boolean = false

  @Inject
  lateinit var tabsManager: TabsManager

  override val layoutRes: Int
    get() = R.layout.activity_more_menu

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fromArticle = intent.getBooleanExtra(EXTRA_KEY_FROM_ARTICLE, false)
    moreMenuList.layoutManager = LinearLayoutManager(this)
    moreMenuList.adapter = MenuListAdapter()
  }


  override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

  inner class MenuListAdapter
  internal
  constructor() : RecyclerView.Adapter<MenuListAdapter.MenuItemHolder>() {
    private val settings = "settings"
    private val history = "history"
    private val addToHomeScreen = "addToHomeScreen"
    private val openWith = "openWith"
    private val article = "article"
    private val tabs = "tabs"
    private val newTab = "newTab"

    private val items = mutableListOf(
      settings,
      tabs,
      newTab,
      history,
      addToHomeScreen,
      openWith,
      article
    )

    init {
      if (fromArticle) {
        items.remove(article)
      }
    }


    override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int
    ) = MenuItemHolder(
      LayoutInflater.from(parent.context).inflate(
        R.layout.activity_more_menu_item_template,
        parent,
        false
      )
    )


    override fun onBindViewHolder(holder: MenuItemHolder, position: Int) {
      val activity = this@ChromerOptionsActivity
      when (items[position]) {
        settings -> {
          holder.menu_image.setImageDrawable(
            IconicsDrawable(holder.itemView.context)
              .icon(CommunityMaterial.Icon.cmd_settings)
              .colorRes(R.color.accent)
              .sizeDp(24)
          )
          holder.menu_text.setText(R.string.settings)
          holder.itemView.setOnClickListener {
            startActivity(
              Intent(
                activity,
                SettingsGroupActivity::class.java
              )
            )
            activity.finish()
          }
        }
        tabs -> {
          holder.menu_image.setImageResource(R.drawable.ic_tabs_24dp)
          holder.menu_text.setText(R.string.title_tabs)
          holder.itemView.setOnClickListener {
            tabsManager.showTabsActivity()
            activity.finish()
          }
        }
        newTab -> {
          holder.menu_image.setImageResource(R.drawable.ic_plus_24dp)
          holder.menu_text.setText(R.string.new_tab_text)
          holder.itemView.setOnClickListener {
            tabsManager.openNewTab(activity, "")
            activity.finish()
          }
        }
        history -> {
          holder.menu_image.setImageDrawable(
            IconicsDrawable(holder.itemView.context)
              .icon(CommunityMaterial.Icon.cmd_history)
              .colorRes(R.color.accent)
              .sizeDp(24)
          )
          holder.menu_text.setText(R.string.title_history)
          holder.itemView.setOnClickListener {
            startActivity(
              Intent(
                activity,
                HistoryActivity::class.java
              )
            )
            activity.finish()
          }
        }
        addToHomeScreen -> {
          holder.menu_image.setImageDrawable(
            IconicsDrawable(holder.itemView.context)
              .icon(CommunityMaterial.Icon.cmd_home_variant)
              .colorRes(R.color.accent)
              .sizeDp(24)
          )
          holder.menu_text.setText(R.string.add_to_homescreen)
          holder.itemView.setOnClickListener {
            startActivity(
              Intent(
                activity,
                HomeScreenShortcutCreatorActivity::class.java
              ).setData(intent.data)
            )
            activity.finish()
          }
        }
        openWith -> {
          holder.menu_image.setImageDrawable(
            IconicsDrawable(holder.itemView.context)
              .icon(CommunityMaterial.Icon.cmd_open_in_new)
              .colorRes(R.color.accent)
              .sizeDp(24)
          )
          holder.menu_text.setText(R.string.open_with)
          holder.itemView.setOnClickListener {
            startActivity(Intent(activity, OpenIntentWithActivity::class.java).apply {
              data = intent.data
              putExtra(EXTRA_KEY_ORIGINAL_URL, intent.dataString)
            })
            activity.finish()
          }
        }
        article -> {
          holder.menu_image.setImageDrawable(
            IconicsDrawable(holder.itemView.context)
              .icon(CommunityMaterial.Icon.cmd_file_document)
              .colorRes(R.color.accent)
              .sizeDp(24)
          )
          holder.menu_text.setText(R.string.open_article_view)
          holder.itemView.setOnClickListener {
            tabsManager.openArticle(
              activity,
              Website(intent.dataString!!)
            )
            activity.finish()
          }
        }
      }
    }

    override fun getItemCount() = items.size

    inner class MenuItemHolder(override val containerView: View) :
      RecyclerView.ViewHolder(containerView), LayoutContainer
  }
}
