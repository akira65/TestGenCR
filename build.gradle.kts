plugins {
    java
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

sourceSets {
    val main by getting {
        java {
            srcDir("src/main/java")
        }
        resources {
            srcDir("src/main/resources")
        }
    }
    val test by getting {
        java {
            srcDir("src/test/java")
        }
        resources {
            srcDir("src/test/resources")
        }
    }
}

dependencies {
    // 必要なJARのみを追加
    implementation(
        files(
            "libs/junit-4.13.2.jar",
            "libs/evosuite-1.2.0.jar",
            "libs/jxplatform-3.0.25.jar",
            "libs/javassist-3.30.2.jar",
            "libs/hamcrest-2.2.jar",
            "libs/slf4j-api-2.0.9.jar",
            "libs/evosuite-standalone-runtime-1.2.0.jar",
            "libs/mockito-core-5.9.0.jar"
        )
    )
    testImplementation(
        files(
            "libs/junit-4.13.2.jar",
            "libs/evosuite-standalone-runtime-1.2.0.jar",
            "libs/mockito-core-5.9.0.jar"
        )
    )
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

application {
    mainClass.set("org.example.testgen_cr.TestGen")
    applicationDefaultJvmArgs = listOf(
        "--illegal-access=deny"
    )
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<JavaExec>("run") {
    mainClass.set("org.example.testgen_cr.TestGen")
    jvmArgs(
        "--illegal-access=deny",
        "--add-opens", "java.desktop/java.awt=ALL-UNNAMED",
        "-Djava.awt.headless=true",
        "-Xss32m"
    )
}
