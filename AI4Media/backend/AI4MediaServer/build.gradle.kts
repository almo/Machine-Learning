import com.google.cloud.tools.gradle.appengine.appyaml.AppEngineAppYamlExtension

val ktor_version: String by project
val kotlin_version: String by project
val gce_logback_version: String by project

plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("com.google.cloud.tools.appengine-appyaml") version "2.8.0"
    id("com.gradleup.shadow") version "8.3.6"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

configure<AppEngineAppYamlExtension> {
    stage {
        setArtifact("build/libs/${project.name}-all.jar")
    }
    deploy {
        version = "alpha001"
        projectId = "meta-gear-464720-g3"
    }
}

dependencies {
    // Google OR-Tools
    implementation("com.google.ortools:ortools-java:9.14.6206")
    implementation("com.google.ortools:ortools-darwin-aarch64:9.14.6206") // For your local development on M1 Mac
    implementation("com.google.ortools:ortools-linux-x86-64:9.14.6206")   // For your deployment target if it's Linux x64

    // Core Google ADK library for Java
    implementation("com.google.adk:google-adk:0.1.0")
    
    // LogBack 
    implementation("com.google.cloud:google-cloud-logging-logback:$gce_logback_version")

    // Kotlin, Ktor and Netty
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
