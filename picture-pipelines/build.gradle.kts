plugins {
    application
    kotlin("jvm") version "2.0.21"
}


group = "ru.nsu.fit.mmp.pipelinesframework"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0")
    implementation("com.github.simoska4.editimage:edit-image:1.1")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.11")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}