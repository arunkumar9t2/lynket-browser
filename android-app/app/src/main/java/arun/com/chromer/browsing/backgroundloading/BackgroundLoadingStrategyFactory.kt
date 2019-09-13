package arun.com.chromer.browsing.backgroundloading

import arun.com.chromer.tabs.ARTICLE
import arun.com.chromer.tabs.CUSTOM_TAB
import arun.com.chromer.tabs.TabType
import arun.com.chromer.tabs.WEB_VIEW
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class BackgroundLoadingStrategyFactory
@Inject
constructor(
        private val articleStrategyProvider: Provider<ArticleBackgroundLoadingStrategy>,
        private val webViewStrategyProvider: Provider<WebViewBackgroundLoadingStrategy>,
        private val customTabsStrategyProvider: Provider<CustomTabBackgroundLoadingStrategy>
) {
    operator fun get(@TabType tabType: Int): BackgroundLoadingStrategy = when (tabType) {
        WEB_VIEW -> webViewStrategyProvider.get()
        CUSTOM_TAB -> customTabsStrategyProvider.get()
        ARTICLE -> articleStrategyProvider.get()
        else -> throw IllegalArgumentException("Invalid tab type")
    }
}