plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        jvmToolchain(8)
        withJava()
    }

    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(project(":kotliny-network-engine"))
            }
        }
        val jvmMain by getting
        val iosMain by getting
    }
}
