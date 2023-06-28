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
        create<MavenPublication>("okHttp") {
            from(components["kotlin"])
            artifact(sourcesJar)
        }
    }
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    compileOnly(project(":kotliny-network-engine"))
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.11.0")
}
