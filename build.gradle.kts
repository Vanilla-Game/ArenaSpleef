plugins {
    id("java")
    id("java-library")
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("com.modrinth.minotaur") version "2.+"
}

group = "org.battleplugins.arena"
version = "2.0.1-SNAPSHOT"

val supportedVersions = listOf(
    "1.19.4",
    "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6",
    "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4"
)

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.battleplugins.org/releases/")
    maven("https://repo.battleplugins.org/snapshots/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    api("org.battleplugins:arena:4.0.0-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
}

tasks {
    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.20.6")

        // Set Java 21 (1.20.6 requires Java 21)
        javaLauncher = project.javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    jar {
        from("src/main/java/resources") {
            include("*")
        }

        archiveFileName.set("ArenaSpleef.jar")
        archiveClassifier.set("")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }
}

modrinth {
    val snapshot = "SNAPSHOT" in rootProject.version.toString()

    token.set(System.getenv("MODRINTH_TOKEN") ?: "")
    projectId.set("arenaspleef")
    versionNumber.set(rootProject.version as String + if (snapshot) "-" + System.getenv("BUILD_NUMBER") else "")
    versionType.set(if (snapshot) "beta" else "release")
    changelog.set(System.getenv("CHANGELOG") ?: "")
    uploadFile.set(tasks.jar)
    gameVersions.set(supportedVersions)

    dependencies {
        required.project("battlearena")
    }
}
