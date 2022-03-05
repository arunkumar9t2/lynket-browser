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

package arun.com.chromer.settings

import arun.com.chromer.search.provider.SearchProviders
import arun.com.chromer.settings.Preferences.*
import com.afollestad.rxkprefs.RxkPrefs
import javax.inject.Inject

const val SEARCH_ENGINE_PREFERENCE = "search_engine_preference"
const val NATIVE_BUBBLES_PREFERENCE = "native_bubbles_preference"

class RxPreferences
@Inject
constructor(rxPrefs: RxkPrefs) {

  val customTabProviderPref by lazy { rxPrefs.string(PREFERRED_CUSTOM_TAB_PACKAGE) }

  val incognitoPref by lazy { rxPrefs.boolean(FULL_INCOGNITO_MODE) }

  val webviewPref by lazy { rxPrefs.boolean(USE_WEBVIEW_PREF) }

  val searchEngine by lazy { rxPrefs.string(SEARCH_ENGINE_PREFERENCE, SearchProviders.GOOGLE) }

  val nativeBubbles by lazy { rxPrefs.boolean(NATIVE_BUBBLES_PREFERENCE, false) }
}
