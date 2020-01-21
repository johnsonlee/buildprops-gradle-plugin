plugins {
    kotlin("jvm") version embeddedKotlinVersion
    kotlin("kapt") version "1.3.21"
    `java-library`
    `maven-publish`
    `signing`

    id("io.codearte.nexus-staging") version "0.21.2"
    id("de.marcphilipp.nexus-publish") version "0.4.0"
}

group = "io.johnsonlee.buildprops"
version = "1.0.0"


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
    implementation("com.google.auto.service:auto-service:1.0-rc6")
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
        register("mavenJava", MavenPublication::class) {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            from(components["java"])

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
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
