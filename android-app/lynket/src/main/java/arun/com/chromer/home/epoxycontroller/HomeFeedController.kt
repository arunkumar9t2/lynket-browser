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

package arun.com.chromer.home.epoxycontroller

import android.app.Application
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.home.epoxycontroller.model.CustomTabProviderInfo
import arun.com.chromer.home.epoxycontroller.model.providerInfo
import arun.com.chromer.home.epoxycontroller.model.recentsCard
import arun.com.chromer.home.epoxycontroller.model.tabsInfo
import arun.com.chromer.shared.epxoy.model.headerLayout
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.epoxy.indeterminateProgress
import com.airbnb.epoxy.AsyncEpoxyController
import dev.arunkumar.common.result.Resource
import dev.arunkumar.common.result.idle
import javax.inject.Inject

class HomeFeedController
@Inject
constructor(
  private val application: Application,
  private val tabsManager: TabsManager
) : AsyncEpoxyController() {

  var customTabProviderInfo: CustomTabProviderInfo? = null
    set(value) {
      field = value
      requestDelayedModelBuild(0)
    }

  var recentWebSites: Resource<List<Website>> = idle()
    set(value) {
      field = value
      requestDelayedModelBuild(0)
    }

  var tabs: List<TabsManager.Tab> = emptyList()
    set(value) {
      field = value
      requestDelayedModelBuild(0)
    }

  override fun buildModels() {
    if (tabs.isNotEmpty() || customTabProviderInfo != null) {
      headerLayout {
        id("status-header")
        title(application.getString(R.string.status))
      }
    }

    if (tabs.isNotEmpty()) {
      tabsInfo {
        id("tabs-info")
        tabs(tabs)
        tabsManager(tabsManager)
      }
    }

    customTabProviderInfo?.let {
      providerInfo {
        id("provider-info")
        providerInfo(it)
      }
    }

    headerLayout {
      id("pages-header")
      title(application.getString(R.string.pages))
    }

    when (val recents = recentWebSites) {
      is Resource.Success -> {
        recentsCard {
          id("recents-card")
          websites(recents.data)
          tabsManager(tabsManager)
        }
      }
      is Resource.Loading -> {
        indeterminateProgress {
          id("recents-progress")
        }
      }
      else -> {}
    }
  }
}
