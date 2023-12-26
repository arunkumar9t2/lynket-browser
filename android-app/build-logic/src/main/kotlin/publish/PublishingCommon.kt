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

package publish

import ModuleVersions
import gradle.ConfigurablePlugin
import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.io.FileInputStream
import java.util.*

public class PublishingCommon : ConfigurablePlugin({
  apply(plugin = "io.github.gradle-nexus.publish-plugin")

  val localProperties: File = file("local.properties")
  if (localProperties.exists()) {
    FileInputStream(localProperties)
      .use { p -> Properties().apply { load(p) } }
      .forEach { key, value -> extra.set(key.toString(), value) }
  } else {
    PublishVariables.forEach { variable ->
      extra[variable] = providers
        .environmentVariable(variable)
        .forUseAtConfigurationTime()
        .getOrElse("")
    }
  }

  val versions = ModuleVersions

  allprojects {
    group = findProperty("groupId").toString()
    if (versions[name] != null) {
      version = if (hasProperty("snapshot")) "main-SNAPSHOT" else versions[name]!!.toString()
    }
  }

  configure<NexusPublishExtension> {
    repositories {
      sonatype {
        stagingProfileId.set(extra[SONATYPE_STAGING_PROFILE_ID].toString())
        username.set(extra[OSSRH_USERNAME].toString())
        password.set(extra[OSSRH_PASSWORD].toString())
        nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
        snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
      }
    }
  }
})
