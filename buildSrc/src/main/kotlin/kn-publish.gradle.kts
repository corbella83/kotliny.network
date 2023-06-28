plugins {
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

private val localPropertiesFile = rootProject.file("credentials.properties")

tasks.dokkaHtml {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

if (localPropertiesFile.exists()) {
    val properties = java.util.Properties()
    properties.load(localPropertiesFile.inputStream())

    val signingKeyId = properties.getProperty("signing.keyId")
    val signingSecretKeyRingFile = properties.getProperty("signing.secretKeyRingFile")
    val signingPassword = properties.getProperty("signing.password")

    val nexusPro = properties.getProperty("nexus.pro_url")
    val nexusSnapshot = properties.getProperty("nexus.snap_url")

    val nexusUsername = properties.getProperty("nexus.username")
    val nexusPassword = properties.getProperty("nexus.password")

    val publishingProperties = listOf(
        signingKeyId,
        signingSecretKeyRingFile,
        signingPassword,
        nexusPro,
        nexusSnapshot,
        nexusUsername,
        nexusPassword
    )

    if (publishingProperties.all { property -> property != null }) {
        val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

        project.extra.set("signing.keyId", signingKeyId)
        project.extra.set("signing.secretKeyRingFile", signingSecretKeyRingFile)
        project.extra.set("signing.password", signingPassword)

        publishing {
            publications.configureEach {
                if (this is MavenPublication) {
                    artifact(dokkaJar)
                    pom {
                        name.set(rootProject.name)
                        description.set("Multiplatform Network Client for Kotlin")
                        url.set("https://github.com/corbella83/kotliny.network")
                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("corbella83")
                                name.set("Pau Corbella")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/corbella83/kotliny.network.git")
                            developerConnection.set("scm:git:ssh://git@github.com/corbella83/kotliny.network.git")
                            url.set("https://github.com/corbella83/kotliny.network")
                        }
                    }
                }
            }

            repositories {
                maven {
                    val releasesRepoUrl = uri(nexusPro)
                    val snapshotsRepoUrl = uri(nexusSnapshot)
                    url = if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl
                    credentials {
                        username = nexusUsername
                        password = nexusPassword
                    }
                }
            }
        }

        tasks.withType<Sign>().configureEach {
            onlyIf { isReleaseVersion }
        }

        signing {
            sign(publishing.publications)
        }
    } else {
        println(
            "Warning: One or more properties required to publish `${project.name}` to Maven Central has not been " +
                    "added to credentials.properties file"
        )
    }
}
