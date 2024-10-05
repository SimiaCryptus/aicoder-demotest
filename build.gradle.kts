fun properties(key: String) = providers.gradleProperty(key).get()

plugins {
    id("java") // Java support
    kotlin("jvm") version "2.0.20"
}

group = "com.github.simiacryptus"
version = properties("pluginVersion")

repositories {
    mavenCentral()
    maven(url = "https://www.jetbrains.com/intellij-repository/releases")
    maven(url = "https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    maven(url = "https://packages.jetbrains.team/maven/p/iuia/qa-automation-maven")
}


val slf4j_version = "2.0.16"
val remoterobot_version = "0.11.23"
val jackson_version = "2.17.2"
val logback_version = "1.4.11"

dependencies {
    implementation("ch.randelshofer:org.monte.media.screenrecorder:17.1")
    implementation("ch.randelshofer:org.monte.media:17.1")
    implementation("ch.randelshofer:org.monte.media.swing:17.1")
    implementation("org.seleniumhq.selenium:selenium-java:4.15.0") {
        exclude(group = "com.intellij.remoterobot", module = "remote-robot")
    }
    implementation("software.amazon.awssdk:bedrock:2.25.7")
    implementation("software.amazon.awssdk:bedrockruntime:2.25.7")

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    implementation("org.seleniumhq.selenium:selenium-java:3.141.59")
    implementation("org.slf4j:slf4j-api:1.7.30")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation(group = "com.simiacryptus", name = "jo-penai", version = "1.1.7")

    implementation(group = "org.apache.httpcomponents.client5", name = "httpclient5", version = "5.3.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    compileOnly(group = "ch.qos.logback", name = "logback-classic", version = logback_version)
    compileOnly(group = "ch.qos.logback", name = "logback-core", version = logback_version)
// https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation("org.jsoup:jsoup:1.18.1")

    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = jackson_version)
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-annotations", version = jackson_version)
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = jackson_version)

    implementation(group = "org.slf4j", name = "slf4j-api", version = slf4j_version)

    implementation(group = "com.intellij.remoterobot", name = "remote-fixtures", version = remoterobot_version)
    implementation(group = "com.intellij.remoterobot", name = "remote-robot", version = remoterobot_version)

    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.12.0")

    implementation(platform("org.junit:junit-bom:5.10.1"))
    implementation("org.junit.jupiter:junit-jupiter")

}

java {
    modularity.inferModulePath.set(false)
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}
tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
sourceSets {
    main {
        kotlin {
            srcDirs("src/main/kotlin")
        }
    }
    test {
        kotlin {
            srcDirs("src/test/kotlin")
        }
    }
}
tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    compilerOptions {
        moduleName.set("com.github.simiacryptus.aicoder.test.demotest")
    }
}

java {
    sourceSets {
        main {
            java {
                srcDirs("src/main/java")
            }
        }
        test {
            java {
                srcDirs("src/test/java")
            }
        }

    }
}

tasks {
    compileJava {
        doLast {
            val servicesDir = File(destinationDirectory.get().asFile, "META-INF/services")
            servicesDir.mkdirs()
            File(servicesDir, "org.monte.media.av.MovieWriterSpi").apply {
                writeText(listOf(
                    "org.monte.media.avi.AVIWriterSpi",
                    "org.monte.media.quicktime.QuickTimeWriterSpi"
                ).joinToString("") { "$it\n"})
            }
            File(servicesDir, "org.monte.media.av.CodecSpi").apply {
                writeText(listOf(
                    "org.monte.media.avi.codec.audio.AVIPCMAudioCodecSpi",
                    "org.monte.media.av.codec.video.TechSmithCodecSpi"
                ).joinToString("") { "$it\n"})
            }
        }
    }

    compileKotlin {
        destinationDirectory.set(compileJava.get().destinationDirectory)
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes(
                "Automatic-Module-Name" to "com.github.simiacryptus.aicoder.demotest"
            )
        }
        from(sourceSets.main.get().output)
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        classpath = sourceSets.test.get().runtimeClasspath

    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            javaParameters.set(true)
            moduleName.set("com.github.simiacryptus.aicoder.test.demotest")
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

}