plugins {
    application
    java
    distribution
    alias(libs.plugins.spotless)
}

application.applicationName = "tipoftheday-demo"
application.mainClass.set("tokyo.northside.swing.demo.TipOfTheDayDemo")

repositories {
    mavenCentral()
}

dependencies {
    implementation(project.rootProject)
    runtimeOnly(libs.slf4j.jdk14)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

sourceSets {
    java {
        main {
            resources {
                setSrcDirs(listOf("docs", "src/main/resources"))
            }
        }
    }
}