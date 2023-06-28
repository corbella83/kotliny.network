plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

val dokkaVersion = "1.8.20"

dependencies {
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
}
