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

tasks.register<JavaExec>("runOnJava17") {
    description = "Launch app on Java 17"
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(application.mainClass)
    jvmArgs(listOf("--add-opens", "java.desktop/sun.awt.X11=ALL-UNNAMED"))
    group = "application"
}
