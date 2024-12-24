plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation(files("libs/junit-4.13.2", "libs/evosuite-standalone-runtime-1.0.6.jar"))
    implementation("org.hamcrest", "hamcrest-core", "1.3")
}

application {
    mainClass.set("org.example.testgen_cr.TestGen")
}

task("generateTests")