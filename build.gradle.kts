import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    application
    kotlin("jvm") version "2.0.21"
}

group = "ru.nsu.fit.mmp.24221"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
}

application { mainClass = "ru.nsu.fit.mmp.24221.mainKt" }

kotlin { jvmToolchain(21) }

sourceSets {
    main {
        java { srcDir("src/main/kotlin") }
        resources { srcDir("src/main/resources") }
    }

    test {
        java { srcDir("src/test/kotlin") }
        resources { srcDir("src/test/resources") }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}
