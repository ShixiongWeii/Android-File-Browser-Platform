plugins {
    application
    kotlin("jvm") version "1.8.20"
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("Main")
}

kotlin {
    jvmToolchain(17)
}

javafx {
    version = "18.0.2"
    modules("javafx.controls", "javafx.graphics")
}
