import com.google.cloud.tools.gradle.appengine.appyaml.AppEngineAppYamlExtension

val ktor_version: String by project
val kotlin_version: String by project
val gce_logback_version: String by project

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

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
    google()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

configure<AppEngineAppYamlExtension> {
    stage {
        setArtifact("build/libs/${project.name}-all.jar")
    }
    deploy {
        version = "alpha002"
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

    // Kotlin, Ktor and Netty
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version") // To make refresh requests
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-client-json:$ktor_version")


    //
    // Firebase
    //
    // Import the BoM (Bill of Materials) to manage versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // The Admin SDK for server-side operations
    implementation("com.google.firebase:firebase-admin:9.2.0")
    
    // Standard App Engine/Servlet dependencies (likely already there)
    implementation("javax.servlet:javax.servlet-api:4.0.1")

    //
    // Logback 
    // 
    implementation("com.google.cloud:google-cloud-logging-logback:$gce_logback_version")
    // 1.- Logback core
    implementation("ch.qos.logback:logback-core:1.4.14")
    // Upgrade Logback to 1.4.x (compatible with Java 11+ and SLF4J 2.x)
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // 2. JSON Layout extensions for Logback
    implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    implementation("ch.qos.logback.contrib:logback-jackson:0.1.5")
    
    // 3. Jackson (Standard JSON processor, likely already in your project, but ensure it's there)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")

    //
    // Google Cloud 
    //
    // Import the BoM (Bill of Materials) to manage versions
    implementation(platform("com.google.cloud:libraries-bom:26.72.0"))
    
    implementation("com.google.cloud:google-cloud-resourcemanager:1.80.0")
    implementation("com.google.cloud:google-cloud-billing:2.80.0")
    
    implementation("com.google.cloud:google-cloud-service-usage")
    implementation("com.google.cloud:google-cloud-tasks")

    implementation("com.google.cloud:google-cloud-secretmanager:2.81.0")
}
