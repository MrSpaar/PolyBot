import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

application {
    mainClass.set("MainKt")
}

group = "kotlin"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.18")
    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")
    implementation("org.litote.kmongo:kmongo:4.7.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.18.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"

}
