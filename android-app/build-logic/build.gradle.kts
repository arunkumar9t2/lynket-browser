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
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
  id("java-gradle-plugin")
  `kotlin-dsl`
  id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

kotlin {
  explicitApi = ExplicitApiMode.Strict
}

gradlePlugin {
  plugins {
    create("androidLibrary") {
      id = "android-library-plugin"
      implementationClass = "android.AndroidLibrary"
    }
    create("androidBinary") {
      id = "android-binary-plugin"
      implementationClass = "android.AndroidBinary"
    }
    create("buildCommon") {
      id = "build-common"
      implementationClass = "common.BuildCommonPlugin"
    }
    create("publishingCommon") {
      id = "publish-common"
      implementationClass = "publish.PublishingCommon"
    }
    create("publishing") {
      id = "publish"
      implementationClass = "publish.PublishingLibrary"
    }
  }
}

dependencies {
  implementation(deps.gradle.dependency.updates)
  implementation(deps.agp)
  implementation(deps.kotlin)
  implementation(deps.spotless)
  implementation(deps.dokka)
  implementation(deps.nexus.publish)
  implementation(deps.kotlinx.binaryvalidator)
  implementation("gradle.plugin.dev.arunkumar:scabbard-gradle-plugin:0.5.0")
}
