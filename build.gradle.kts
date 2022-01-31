import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "com.sarahisweird"
version = "1.0.1"

repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.23.0-SNAPSHOT")
    implementation("dev.schlaubi.lavakord:java:3.3.0")
    implementation("dev.schlaubi.lavakord:kord:3.3.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

application {
    mainClass.set("com.sarahisweird.uwubot.MainKt")
}