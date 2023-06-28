buildscript {
    val kotlinVersion = "1.8.22"
    val dokkaVersion = "1.8.20"

    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(kotlin("serialization", kotlinVersion))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    }
}

allprojects {
    val libraryGroup: String by project
    val libraryVersion: String by project

    group = libraryGroup
    version = libraryVersion

    repositories {
        mavenCentral()
        google()
    }
}
