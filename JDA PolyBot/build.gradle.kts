import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    application
}

application {
    mainClass.set("MainKt")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "17"
    }
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.18")
    implementation("com.sedmelluq:lavaplayer:1.3.77")
    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")
    implementation("org.xerial:sqlite-jdbc:3.39.3.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel-gson:2.3.1")
    implementation("com.google.code.gson:gson:2.9.1")
}
