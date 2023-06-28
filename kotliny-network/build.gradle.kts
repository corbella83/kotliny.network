plugins {
    kotlin("multiplatform")
    id("kn-publish")
}

kotlin {
    jvm {
        jvmToolchain(8)
        withJava()
        testRuns["test"].executionTask.configure { useJUnitPlatform() }
    }

    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":kotliny-network-engine"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":kotliny-network-engine-test"))
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting

        val iosMain by getting
        val iosTest by getting
    }
}

tasks.register("runIosTests") {
    val device = project.findProperty("iosDevice") as? String ?: "iPhone 14 Pro"
    dependsOn("linkDebugTestIosX64")
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Runs tests for target 'ios' on an iOS simulator"

    doLast {
        val binary = (kotlin.targets["iosX64"] as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget).binaries.getTest("DEBUG").outputFile
        exec {
            commandLine("xcrun", "simctl", "spawn", device, binary.absolutePath)
        }
    }
}
