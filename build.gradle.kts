import java.io.FileInputStream
import java.util.Properties

plugins {
    `java-library`
    `maven-publish`
    distribution
    jacoco
    signing
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.spotless)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.sphinx)
    alias(libs.plugins.git.version) apply false
}

val dotgit = project.file(".git")
if (dotgit.exists()) {
    apply(plugin = libs.plugins.git.version.get().pluginId)
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    val details = versionDetails()
    val baseVersion = details.lastTag.substring(1)
    version = when {
        details.isCleanTag -> baseVersion
        else -> baseVersion + "-" + details.commitDistance + "-" + details.gitHash + "-SNAPSHOT"
    }
} else {
    val gitArchival = project.file(".git-archival.properties")
    val props = Properties()
    props.load(FileInputStream(gitArchival))
    val versionDescribe = props.getProperty("describe")
    val regex = "^v\\d+\\.\\d+\\.\\d+$".toRegex()
    version = when {
        regex.matches(versionDescribe) -> versionDescribe.substring(1)
                                            else -> versionDescribe.substring(1) + "-SNAPSHOT"
    }
}

group = "tokyo.northside"

repositories {
    mavenLocal()
    mavenCentral()
}

val integrationTest by sourceSets.creating

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(libs.swingbox)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.slf4j.jdk14)
    "integrationTestImplementation"(project)
    "integrationTestImplementation"(libs.assertj.swing.junit)
    "integrationTestImplementation"(libs.slf4j.jdk14)
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = integrationTest.output.classesDirs
    classpath = configurations[integrationTest.runtimeClasspathConfigurationName] + integrationTest.output

    shouldRunAfter(tasks.test)
}

/*
tasks.named("check") {
    dependsOn(integrationTestTask)
}
*/

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "tokyo.northside.tipoftheday")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    // Test in headless mode with ./gradlew test -Pheadless
    if (project.hasProperty("headless")) {
        systemProperty("java.awt.headless", "true")
    }
    testLogging.setShowStandardStreams(true)
    testLogging.setExceptionFormat("full")
}

jacoco {
    toolVersion="0.8.6"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:unchecked")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("tipoftheday")
                description.set("Java Swing Tip of the Day component library")
                url.set("https://codeberg.org/miurahr/tipoftheday")
                licenses {
                    license {
                        name.set("The GNU Lesser General Public License, Version 2.1")
                        url.set("https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("miurahr")
                        name.set("Hiroshi Miura")
                        email.set("miurahr@linux.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://codeberg.org/miurahr/tipoftheday.git")
                    developerConnection.set("scm:git:git://codeberg.org/miurahr/tipoftheday.git")
                    url.set("https://codeberg.org/miurahr/tipoftheday")
                }
            }
        }
    }
}

val signKey = listOf("signingKey", "signing.keyId", "signing.gnupg.keyName").find {project.hasProperty(it)}
tasks.withType<Sign> {
    onlyIf { signKey != null && !project.version.toString().endsWith("-SNAPSHOT") }
}
signing {
    when (signKey) {
        "signingKey" -> {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
        "signing.keyId" -> {
            val keyId: String? by project
            val password: String? by project
            val secretKeyRingFile: String? by project // e.g. gpg --export-secret-keys > secring.gpg
            useInMemoryPgpKeys(keyId, password, secretKeyRingFile)
        }
        "signing.gnupg.keyName" -> {
            useGpgCmd()
        }
    }
    sign(publishing.publications["mavenJava"])
}

val sonatypeUsername: String? by project
val sonatypePassword: String? by project

// ---------- publish to sonatype OSSRH
nexusPublishing {
    repositories.sonatype {
        if (sonatypeUsername != null && sonatypePassword != null) {
            username.set(sonatypeUsername)
            password.set(sonatypePassword)
        } else {
            username.set(System.getenv("SONATYPE_USER"))
            password.set(System.getenv("SONATYPE_PASS"))
        }
    }
}

spotless {
    format("misc") {
        target(listOf("*.gradle", ".gitignore", "*.md", "*.rst"))
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    java {
        target(listOf("src/*/java/**/*.java"))
        palantirJavaFormat()
        importOrder()
        removeUnusedImports()
        formatAnnotations()
    }
}

spotbugs {
    excludeFilter.set(project.file("config/spotbugs/exclude.xml"))
    tasks.spotbugsMain {
        reports.create("html") {
            required.set(true)
        }
    }
    tasks.spotbugsTest {
        reports.create("html") {
            required.set(true)
        }
    }
}

tasks.sphinx {
    sourceDirectory {"docs"}
}
