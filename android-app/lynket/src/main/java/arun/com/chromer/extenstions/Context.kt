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

package arun.com.chromer.extenstions

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.annotation.StringRes

/**
 * Created by Arunkumar on 10-12-2017.
 */

/**
 * Returns true if the given package was installed
 */
fun PackageManager.isPackageInstalled(packageName: String?): Boolean {
  if (packageName == null) return false
  return try {
    this.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    true
  } catch (e: PackageManager.NameNotFoundException) {
    false
  }
}

fun Context.appName(packageName: String): String {
  val pm = applicationContext.packageManager
  val ai: ApplicationInfo? = try {
    pm.getApplicationInfo(packageName, 0)
  } catch (e: PackageManager.NameNotFoundException) {
    null
  }
  return (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)") as String
}

data class StringResource(
  @param:StringRes val resource: Int,
  val args: List<String> = emptyList(),
  val resourceArgs: List<Int> = emptyList()
)

fun Context.resolveStringResource(stringResource: StringResource): String {
  return when {
    stringResource.args.isNotEmpty() -> getString(
      stringResource.resource,
      *stringResource.args.toTypedArray()
    )
    stringResource.resourceArgs.isNotEmpty() -> getString(
      stringResource.resource,
      *stringResource.resourceArgs.map(::getString).toTypedArray()
    )
    else -> getString(stringResource.resource)
  }
}
