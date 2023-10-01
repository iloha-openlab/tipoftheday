import java.io.File
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

fun getProps(f: File): Properties {
    val props = Properties()
    try {
        props.load(FileInputStream(f))
    } catch (t: Throwable) {
        println("Can't read $f: $t, assuming empty")
    }
    return props
}

// we handle cases without .git directory
val home = System.getProperty("user.home")
val javaHome = System.getProperty("java.home")
val props = project.file("src/main/resources/version.properties")
val dotgit = project.file(".git")
if (dotgit.exists()) {
    apply(plugin = "com.palantir.git-version")
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    val details = versionDetails()
    val baseVersion = details.lastTag.substring(1)
    version = if (details.isCleanTag) {  // release version
        baseVersion
    } else {  // snapshot version
        baseVersion + "-" + details.commitDistance + "-" + details.gitHash + "-SNAPSHOT"
    }
} else if (props.exists()) { // when version.properties already exist, just use it.
    version = getProps(props).getProperty("version")
}

tasks.register("writeVersionFile") {
    val folder = project.file("src/main/resources")
    if (!folder.exists()) {
        folder.mkdirs()
    }
    props.delete()
    props.appendText("version=" + project.version)
}

tasks.getByName("jar") {
    dependsOn("writeVersionFile")
}

group = "tokyo.northside"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.swingbox)
    testImplementation(libs.junit.jupiter)
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
}

java {
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

nexusPublishing {
    repositories{
        sonatype()
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
