plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.croxln"
version = "1.0.0"
description = "A lightweight, modern utility and foundation library for PaperMC (1.21+) and Folia plugins."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:26.0.1")
    
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:-processing"))
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as? StandardJavadocDocletOptions)?.apply {
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set("Paper Dev Toolkit")
                description.set("Lightweight Java utilities, Adventure MiniMessage helpers, Folia schedulers, and GUI framework for modern PaperMC plugins.")
                url.set("https://github.com/croxln/paper-dev-toolkit")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("croxln")
                        name.set("croxln")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/croxln/paper-dev-toolkit.git")
                    developerConnection.set("scm:git:ssh://github.com:croxln/paper-dev-toolkit.git")
                    url.set("https://github.com/croxln/paper-dev-toolkit")
                }
            }
        }
    }
}
