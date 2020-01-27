plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm").version("1.3.31")
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    val lynketBuild by plugins.creating {
        id = "lynket-build"
        implementationClass = "dev.arunkumar.lynket.buildplugin.BuildPlugin"
    }
}
