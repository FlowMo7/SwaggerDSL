import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.3.40"
    id("org.jetbrains.dokka") version "0.9.18"
    id("com.jfrog.bintray") version "1.8.4"
    id("com.github.ben-manes.versions") version "0.21.0"
}

group = "dev.moetz"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    val okHttpVersion = "3.14.2"
    val kotlinVersion = "1.3.40"
    val mockkVersion = "1.9.3"
    val kluentVersion = "1.50"

    compile(kotlin("stdlib"))

    compile("com.squareup.okhttp3:okhttp:$okHttpVersion")
    compile("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    testCompile("junit:junit:4.12")
    testCompile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testCompile("io.mockk:mockk:$mockkVersion")
    testCompile("org.amshove.kluent:kluent:$kluentVersion")
    testCompile("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
    jdkVersion = 8
}


val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(dokka)
}

// Create sources Jar from main kotlin sources
val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    classifier = "sources"
    from(kotlin.sourceSets["main"].kotlin.srcDirs)
}


fun MavenPom.addDependencies() = withXml {
    asNode().appendNode("dependencies").let { depNode ->
        configurations.compile.allDependencies.forEach {
            depNode.appendNode("dependency").apply {
                appendNode("groupId", it.group)
                appendNode("artifactId", it.name)
                appendNode("version", it.version)
            }
        }
    }
}


fun findStringProperty(s: String) = project.findProperty(s) as? String

bintray {
    user = findStringProperty("bintray.user")
    key = findStringProperty("bintray.apikey")

    setConfigurations("archives")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = findStringProperty("bintray.repo")
        name = findStringProperty("bintray.name")
        desc = findStringProperty("library.description")
        websiteUrl = findStringProperty("library.siteURL")
        vcsUrl = findStringProperty("library.gitURL")
        setLicenses(findStringProperty("licenses.allLicenses"))
        dryRun = true
        publish = true
        override = false
        publicDownloadNumbers = true
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            desc = findStringProperty("library.description")
        })
    })
}