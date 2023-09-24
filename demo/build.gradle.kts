plugins {
    application
    java
    distribution
    alias(libs.plugins.spotless)
}

application.applicationName = "demo"
application.mainClass.set("tokyo.northside.swing.demo.TipOfTheDayDemo")

repositories {
    mavenCentral()
}

dependencies {
    implementation(project.rootProject)
    implementation(libs.swingbox)
    runtimeOnly(libs.slf4j.simple)
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