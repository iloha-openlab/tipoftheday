import java.io.File
import java.io.FileInputStream
import java.util.Properties

plugins {
    application
    `java-library`
    `maven-publish`
    checkstyle
    jacoco
    signing
    id("com.github.spotbugs") version "5.0.9"
    id("com.diffplug.spotless") version "6.15.0"
    id("tokyo.northside.sphinx") version "1.0.4"
    id("com.palantir.git-version") version "0.13.0" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
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
    if (details.isCleanTag) {  // release version
        version = baseVersion
    } else {  // snapshot version
        version = baseVersion + "-" + details.commitDistance + "-" + details.gitHash + "-SNAPSHOT"
    }
} else if (props.exists()) { // when version.properties already exist, just use it.
    version = getProps(props).getProperty("version")
}

tasks.register("writeVersionFile") {
    val folder = project.file("src/main/resources");
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
    mavenLocal()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    // Test in headless mode with ./gradlew test -Pheadless
    if (project.hasProperty("headless")) {
        systemProperty("java.awt.headless", "true")
    }
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
        sonatype {
            username.set(project.property("sonatypeUsername")?.toString()?: System.getenv("SONATYPE_USER"))
            password.set(project.property("sonatypePassword")?.toString()?: System.getenv("SONATYPE_PASS"))
        }
    }
}

spotless {
    format("misc") {
        target(listOf("*.gradle", ".gitignore", "*.rst"))
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    format("styling") {
        target(listOf("*.md"))
        prettier()
    }
    java {
        target(listOf("src/*/java/**/*.java"))
        palantirJavaFormat()
        importOrder()
        removeUnusedImports()
        formatAnnotations()
    }
}

tasks.sphinx {
    sourceDirectory {"docs"}
}

project(":demo") {
    apply(plugin = "application")
    application.applicationName = "demo"
    application.mainClass.set("tokyo.northside.swing.demo.TipOfTheDayDemo")
    dependencies {
        implementation(project.rootProject)
    }
}