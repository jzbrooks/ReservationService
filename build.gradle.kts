plugins {
    id("io.ktor.plugin") version "2.2.4"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

group = "com.jzbrooks"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.jzbrooks.reservations.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("ReservationService.jar")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.exposed)
    implementation(libs.hikari)
    implementation(libs.postgres)
    implementation(libs.logback)
    testImplementation(kotlin("test"))
    testImplementation(libs.h2)
    testImplementation(libs.assertk)
    testImplementation(libs.ktCoroutinesTest)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
