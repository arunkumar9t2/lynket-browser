package arun.com.chromer.home.epoxycontroller

import arun.com.chromer.home.epoxycontroller.model.CustomTabProviderInfo
import arun.com.chromer.home.epoxycontroller.model.providerInfo
import com.airbnb.epoxy.AsyncEpoxyController

class HomeFeedController : AsyncEpoxyController() {

    var customTabProviderInfo: CustomTabProviderInfo? = null
        set(value) {
            field = value
            requestModelBuild()
            requestDelayedModelBuild(0)
        }

    override fun buildModels() {
        customTabProviderInfo?.let {
            providerInfo {
                id("provider-info")
                providerInfo(it)
            }
        }
    }
}