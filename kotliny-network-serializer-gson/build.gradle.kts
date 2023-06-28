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
        create<MavenPublication>("gson") {
            from(components["kotlin"])
            artifact(sourcesJar)
        }
    }
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(project(":kotliny-network-api-caller"))
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(project(":kotliny-network-engine-test"))
    testImplementation(kotlin("test"))
}
