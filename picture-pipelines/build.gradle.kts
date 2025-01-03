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
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}