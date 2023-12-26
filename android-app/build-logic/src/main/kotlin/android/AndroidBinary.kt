/*
 *
 *  Lynket
 *
 *  Copyright (C) 2023 Arunkumar
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

package android

import ANDROID_PACKAGE_NAME
import ANDROID_VERSION_CODE
import ANDROID_VERSION_NAME
import com.android.build.api.dsl.ApplicationExtension
import gradle.ConfigurablePlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

public class AndroidBinary : ConfigurablePlugin({
  apply(plugin = "com.android.application")

  androidCommon()

  configure<ApplicationExtension> {
    defaultConfig {
      applicationId = ANDROID_PACKAGE_NAME
      versionCode = ANDROID_VERSION_CODE
      versionName = ANDROID_VERSION_NAME
    }
  }
})

