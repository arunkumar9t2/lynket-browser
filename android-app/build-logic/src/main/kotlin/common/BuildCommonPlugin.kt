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

package common

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import gradle.ConfigurablePlugin
import gradle.deps
import gradle.version
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaPlugin
import com.github.benmanes.gradle.versions.VersionsPlugin as GradleVersionsPlugin

/**
 * Common build plugin that should be applied to root `build.gradle` file. This plugin can be used
 * to add logic that is meant to be added to all subprojects in the current build.
 *
 * Example
 * ```kotlin
 *  plugins {
 *    id "build-common"
 *  }
 * ```
 *
 * Note: To limit cross configuration, only logic that absolutely need to exist such as linting and
 * similar configuration should be added here. For domain specific build logic, prefer to create
 * dedicated plugins and apply them using `plugins {}` block.
 *
 * Ideally we would like to cross configuration all together but it is still convenient when we need
 * to configure all projects at a single place. If Gradle is evalue root build.gradle differently then
 * it would be best of both worlds.
 */
public class BuildCommonPlugin : ConfigurablePlugin({
  if (this != rootProject) {
    error("build-common should be only applied to root project")
  }
  configureDokka()

  subprojects {
    configureSpotless()
  }

  apply<GradleVersionsPlugin>()
})

/**
 * Configures spotless plugin on given subproject.
 */
private fun Project.configureSpotless() {
  apply<SpotlessPlugin>()
  configure<SpotlessExtension> {
    kotlin {
      targetExclude("$buildDir/**/*.kt", "bin/**/*.kt")

      ktlint(deps.version("ktlint")).userData(
        mapOf(
          "indent_size" to "2",
          "continuation_indent_size" to "2",
          "disabled_rules" to "no-wildcard-imports"
        )
      )
    }
  }
}

private fun Project.configureDokka() {
  apply<DokkaPlugin>()
}

