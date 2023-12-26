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

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceDirectorySet
import gradle.ConfigurablePlugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaPlugin


public class PublishingLibrary : ConfigurablePlugin({
  apply(plugin = "maven-publish")
  apply(plugin = "signing")

  val isAndroid = project.plugins.hasPlugin("com.android.library")

  configureDokka()

  // Setup sources jar
  val sourceJarTask = registerSourceJarTask()
  // Documentation
  val javaDocsTask = registerJavaDocsTask()

  artifacts {
    add("archives", sourceJarTask)
    add("archives", javaDocsTask)
  }

  val website = findProperty("website").toString()

  // Setup publishing
  afterEvaluate {
    configure<PublishingExtension> {
      publications {
        create<MavenPublication>("release") {
          groupId = findProperty("groupId").toString()
          artifactId = project.name
          version = project.version.toString()

          if (isAndroid) {
            from(components["release"])
          } else {
            from(components["java"])
          }

          artifact(sourceJarTask)
          artifact(javaDocsTask)

          pom {
            name.set(project.name)
            description.set(findProperty("description").toString())
            url.set(website)

            licenses {
              license {
                name.set("Apache License, Version 2.0")
                //TODO Update License URL
                url.set("")
              }
            }

            developers {
              developer {
                //TODO Update developer details
                id.set("arunkumar9t2")
                name.set("Arunkumar")
                email.set("hi@arunkumar.dev")
              }
            }

            scm {
              connection.set("${website}.git")
              developerConnection.set("${website}.git")
              url.set(website)
            }
          }
        }
      }
    }
  }

  configureSigning()
})

private fun Project.configureDokka() {
  apply<DokkaPlugin>()
}

private fun Project.registerJavaDocsTask(): TaskProvider<Jar> {
  val javaDocsTask = tasks.register<Jar>("javadocJar")
  val dokkaJavaDocTask = tasks.named<AbstractDokkaTask>("dokkaJavadoc")
  javaDocsTask.configure {
    archiveClassifier.set("javadoc")
    dependsOn(dokkaJavaDocTask)
    from(dokkaJavaDocTask.map { it.outputDirectory })
  }
  return javaDocsTask
}

private fun Project.registerSourceJarTask(): TaskProvider<Jar> {
  val sourcesJar = "sourcesJar"
  val isAndroid = project.plugins.hasPlugin("com.android.library")
  val android = extensions.getByType<BaseExtension>()

  return tasks.register<Jar>(sourcesJar) {
    archiveClassifier.set("sources")
    if (isAndroid) {
      from(project.provider {
        android
          .sourceSets
          .matching { it.name == "main" }
          .flatMap { it.java.srcDirs + (it.kotlin as AndroidSourceDirectorySet).srcDirs }
      })
    } else {
      extensions.findByType<SourceSetContainer>()
        ?.getByName("main")
        ?.allJava
        ?.srcDirs
        ?.let { from(it) }
    }
  }
}

private fun Project.configureSigning() {
  configure<SigningExtension> {
    useInMemoryPgpKeys(
      rootProject.extra[SIGNING_KEY_ID].toString(),
      rootProject.extra[SIGNING_KEY].toString(),
      rootProject.extra[SIGNING_PASSWORD].toString(),
    )
    sign(the<PublishingExtension>().publications)
  }
}
