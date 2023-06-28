plugins {
    id("java-library")
    id("kotlin")
    id("kn-publish")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("jvm11") {
            from(components["kotlin"])
            artifact(sourcesJar)
        }
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    compileOnly(project(":kotliny-network-engine"))
}
