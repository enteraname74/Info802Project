import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion

    val graphQlVersion: String by System.getProperties()
    id("com.apollographql.apollo3") version "3.8.2"
}

version = "1.0.0-SNAPSHOT"
group = "com.github.enteraname74"

repositories {
    mavenCentral()
    mavenLocal()
}

// Versions
val kotlinVersion: String by System.getProperties()
val kvisionVersion: String by System.getProperties()
val graphQlVersion: String by System.getProperties()

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig(Action {
                outputFileName = "main.bundle.js"
            })
            runTask(Action {
                sourceMaps = false
                devServer = KotlinWebpackConfig.DevServer(
                    open = false,
                    port = 3000,
                    proxy = mutableMapOf(
                        "/kv/*" to "http://localhost:8080",
                        "/kvsse/*" to "http://localhost:8080",
                        "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                    ),
                    static = mutableListOf("${layout.buildDirectory.asFile.get()}/processedResources/js/main")
                )
            })
            testTask(Action {
                useKarma {
                    useChromeHeadless()
                }
            })
        }
        binaries.executable()
    }
    sourceSets["jsMain"].dependencies {
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
        implementation("io.kvision:kvision-maps:$kvisionVersion")
        implementation("io.kvision:kvision-rest:$kvisionVersion")
        implementation("io.kvision:kvision-state:$kvisionVersion")
        implementation(npm("leaflet-routing-machine", "3.2.12"))
        implementation(npm("left-pad", "1.3.0"))
        implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")
        implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
        // https://mvnrepository.com/artifact/com.google.code.gson/gson
        implementation("com.google.code.gson:gson:2.10.1")

    }
    sourceSets["jsTest"].dependencies {
        implementation(kotlin("test-js"))
        implementation("io.kvision:kvision-testutils:$kvisionVersion")
    }
}

apollo {
    service("service") {
        packageName.set("com.github.enteraname74")
    }
}
