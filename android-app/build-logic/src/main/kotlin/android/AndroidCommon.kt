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

import ANDROID_COMPILE_SDK
import ANDROID_DEBUG_VARIANT
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
      sourceCompatibility(JavaVersion.VERSION_11)
      targetCompatibility(JavaVersion.VERSION_11)
    }

    defaultConfig {
      minSdk = ANDROID_MIN_SDK
      targetSdk = ANDROID_TARGET_SDK

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

      vectorDrawables {
        useSupportLibrary = true
      }

      multiDexEnabled = true
    }

    buildTypes {

      named(ANDROID_RELEASE_VARIANT) {
        isMinifyEnabled = true
        isShrinkResources = true
        isDebuggable = false

        proguardFiles(
          getDefaultProguardFile(name = "proguard-android-optimize.txt"),
          "proguard-rules.pro"
        )
      }

      named(ANDROID_DEBUG_VARIANT) {
        isMinifyEnabled = false
        isShrinkResources = false
        isDebuggable = true
      }
    }

    buildTypes.configureEach {
      javaCompileOptions
        .annotationProcessorOptions
        .arguments += listOf(
        "logEpoxyTimings" to "true",
        "enableParallelEpoxyProcessing" to "true"
      )
    }

    composeOptions {
      kotlinCompilerExtensionVersion = deps.version("compose")!!
    }

    packagingOptions {
      resources.excludes += listOf(
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "META-INF/licenses/**",
        "META-INF/rxjava.properties"
      )
    }

    lintOptions {
      isAbortOnError = false
      disable.add("MissingTranslation")
    }

    testOptions {
      unitTests {
        isIncludeAndroidResources = true
      }
    }
  }

  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      jvmTarget = "11"
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
