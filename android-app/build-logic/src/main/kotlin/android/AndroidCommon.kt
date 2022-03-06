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

package android

import ANDROID_COMPILE_SDK
import ANDROID_MIN_SDK
import ANDROID_RELEASE_VARIANT
import ANDROID_TARGET_SDK
import gradle.deps
import gradle.version
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.androidCommon() {
  apply(plugin = "org.jetbrains.kotlin.android")

  android {
    compileSdkVersion(ANDROID_COMPILE_SDK)

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_1_8
      targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
      minSdk = ANDROID_MIN_SDK
      targetSdk = ANDROID_TARGET_SDK

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      vectorDrawables {
        useSupportLibrary = true
      }
    }

    buildTypes {
      named(ANDROID_RELEASE_VARIANT) {
        proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
        )
      }
    }

    composeOptions {
      kotlinCompilerExtensionVersion = deps.version("compose")!!
    }

    packagingOptions {
      resources.excludes += listOf(
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "META-INF/licenses/**"
      )
    }

    lintOptions {
      isAbortOnError = false
    }
  }

  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs += listOf(
        "-Xopt-in=kotlin.ExperimentalStdlibApi",
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xopt-in=kotlin.time.ExperimentalTime",
        "-Xopt-in=kotlin.experimental.ExperimentalTypeInference",
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      )
    }
  }
}
