plugins {
    `java-library`
    `java-gradle-plugin`
    `signing`

    id("com.gradle.plugin-publish") version "0.12.0"
    id("de.marcphilipp.nexus-publish") version "0.4.0"
    id("io.codearte.nexus-staging") version "0.21.2"

    kotlin("jvm") version embeddedKotlinVersion
    kotlin("kapt") version embeddedKotlinVersion
}

group = "io.johnsonlee.buildprops"
version = "1.1.0"
description = "Generate Build.java for module"


repositories {
    mavenLocal()
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    kapt("com.google.auto.service:auto-service:1.0-rc6")
    implementation(gradleApi())
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
    classifier = "javadoc"
    from(tasks["javadoc"])
}

val OSSRH_USERNAME = project.properties["OSSRH_USERNAME"] as? String ?: System.getenv("OSSRH_USERNAME")
val OSSRH_PASSWORD = project.properties["OSSRH_PASSWORD"] as? String ?: System.getenv("OSSRH_PASSWORD")

gradlePlugin {
    plugins {
        create("buildPropsPlugin") {
            id = "io.johnsonlee.buildprops"
            implementationClass = "io.johnsonlee.buildprops.BuildPropsPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/johnsonlee/${project.name}"
    vcsUrl = "https://github.com/johnsonlee/${project.name}"
    description = project.description

    (plugins) {
        "buildPropsPlugin" {
            displayName = project.name
            description = project.description
            tags = listOf("build", "gradle", "kotlin")
            version = "${project.version}"
        }
    }

    mavenCoordinates {
        groupId = "${project.group}"
        artifactId = project.name
        version = "${project.version}"
    }
}

nexusStaging {
    packageGroup = "io.johnsonlee"
    username = OSSRH_USERNAME
    password = OSSRH_PASSWORD
    numberOfRetries = 50
    delayBetweenRetriesInMillis = 3000
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(OSSRH_USERNAME)
            password.set(OSSRH_PASSWORD)
        }
    }
}


publishing {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
    publications {
        withType(MavenPublication::class.java).configureEach {
            val publication = this
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            if ("mavenJava" == publication.name) {
                from(components["java"])
            }

            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            pom.withXml {
                asNode().apply {
                    appendNode("name", project.name)
                    appendNode("url", "https://github.com/johnsonlee/buildprops-gradle-plugin")
                    appendNode("description", project.description ?: project.name)
                    appendNode("scm").apply {
                        appendNode("connection", "scm:git:git://github.com/johnsonlee/buildprops-gradle-plugin.git")
                        appendNode("developerConnection", "scm:git:git@github.com:johnsonlee/buildprops-gradle-plugin.git")
                        appendNode("url", "https://github.com/johnsonlee/buildprops-gradle-plugin")
                    }
                    appendNode("licenses").apply {
                        appendNode("license").apply {
                            appendNode("name", "Apache License")
                            appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    appendNode("developers").apply {
                        appendNode("developer").apply {
                            appendNode("id", "johnsonlee")
                            appendNode("name", "Johnson Lee")
                            appendNode("email", "g.johnsonlee@gmail.com")
                        }
                    }
                }
            }

            project.signing {
                sign(publication)
            }
        }
    }
}
