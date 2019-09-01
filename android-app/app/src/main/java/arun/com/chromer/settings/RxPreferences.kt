package arun.com.chromer.settings

import arun.com.chromer.search.provider.SearchProviders
import arun.com.chromer.settings.Preferences.*
import com.afollestad.rxkprefs.RxkPrefs
import javax.inject.Inject

const val SEARCH_ENGINE_PREFERENCE = "search_engine_preference"

class RxPreferences
@Inject
constructor(rxPrefs: RxkPrefs) {
    val customTabProviderPref by lazy { rxPrefs.string(PREFERRED_CUSTOM_TAB_PACKAGE) }

    val incognitoPref by lazy { rxPrefs.boolean(FULL_INCOGNITO_MODE) }

    val webviewPref by lazy { rxPrefs.boolean(USE_WEBVIEW_PREF) }

    val searchEngine by lazy { rxPrefs.string(SEARCH_ENGINE_PREFERENCE, SearchProviders.GOOGLE) }
}