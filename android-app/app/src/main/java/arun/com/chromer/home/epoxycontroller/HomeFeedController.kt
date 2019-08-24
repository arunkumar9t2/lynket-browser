package arun.com.chromer.home.epoxycontroller

import android.app.Application
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.home.epoxycontroller.model.*
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.epoxy.indeterminateProgress
import com.airbnb.epoxy.AsyncEpoxyController
import dev.arunkumar.common.result.Result
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

    var recentWebSites: Result<List<Website>> = idle()
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
            is Result.Success -> {
                recentsCard {
                    id("recents-card")
                    websites(recents.data)
                    tabsManager(tabsManager)
                }
            }
            is Result.Loading -> {
                indeterminateProgress {
                    id("recents-progress")
                }
            }
        }
    }
}