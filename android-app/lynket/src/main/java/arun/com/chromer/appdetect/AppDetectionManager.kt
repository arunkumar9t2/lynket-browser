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

package arun.com.chromer.appdetect

import android.app.Application
import android.text.TextUtils
import arun.com.chromer.settings.Preferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Arunkumar on 21-01-2017.
 */
@Singleton
class AppDetectionManager
@Inject
constructor(private val application: Application) {

  // Last detected package name of app;
  var filteredPackage = ""

  @get:Synchronized
  var nonFilteredPackage = ""
    private set

  @Synchronized
  fun logPackage(appPackage: String) {
    if (TextUtils.isEmpty(appPackage)) {
      return
    }
    if (!nonFilteredPackage.equals(appPackage, ignoreCase = true) && nonFilterPackage(appPackage)) {
      nonFilteredPackage = appPackage
    }
    if (!filteredPackage.equals(appPackage, ignoreCase = true) && filterPackage(appPackage)) {
      filteredPackage = appPackage
    }
    Timber.d("Current package: %s", appPackage)
  }

  @Synchronized
  fun clear() {
    nonFilteredPackage = ""
    filteredPackage = nonFilteredPackage
  }


  private fun nonFilterPackage(appPackage: String): Boolean {
    return if (appPackage.equals("android", ignoreCase = true)) {   // Ignore system pop ups
      false
    } else !appPackage.equals(application.packageName, ignoreCase = true) // Ignore our app

  }

  private fun filterPackage(packageName: String): Boolean {
    // Ignore system pop ups
    if (packageName.equals("android", ignoreCase = true)) return false

    // Ignore system pop ups
    if (packageName.contains("systemui")) return false

    // Ignore our app
    if (packageName.equals(application.packageName, ignoreCase = true)) return false

    // Chances are that we picked the opening custom tab, so let's ignore our default provider
    // to be safe
    if (packageName.equals(
        Preferences.get(application).customTabPackage()!!,
        ignoreCase = true
      )
    ) return false

    // Ignore google quick search box
    return !packageName.equals("com.google.android.googlequicksearchbox", ignoreCase = true)
    // There can also be cases where there is no default provider set, so lets ignore all possible
    // custom tab providers to be sure. This is safe since browsers don't call our app anyways.

    // Commenting, research needed
    // if (mCustomTabPackages.contains(packageName)) return true;
  }
}
