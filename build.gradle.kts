import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    java
    eclipse
    idea
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
    id("xyz.jpenilla.run-velocity") version "2.3.1"
}

group = "top.ourisland"
version = "0.2.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:3.1.0")

    implementation("org.yaml:snakeyaml:2.5")
}

tasks {
    runVelocity {
        velocityVersion("3.5.0-SNAPSHOT")
    }
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")

val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)

    from(file("src/main/templates"))
    into(layout.buildDirectory.dir("generated/sources/templates"))
    expand(props)
}

sourceSets.main.get().java.srcDir(generateTemplates.map { it.outputs })

idea {
    project {
        settings {
            taskTriggers {
                afterSync(generateTemplates)
            }
        }
    }
}
eclipse.synchronizationTasks(generateTemplates)
