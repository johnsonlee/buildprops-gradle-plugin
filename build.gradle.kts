import org.gradle.api.Project.DEFAULT_VERSION

buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

plugins {
    `java-library`
    `java-gradle-plugin`
    kotlin("jvm") version embeddedKotlinVersion
    kotlin("kapt") version embeddedKotlinVersion
    id("io.johnsonlee.sonatype-publish-plugin") version "1.5.6"
}

group = "io.johnsonlee.buildprops"
version = project.findProperty("version")?.takeIf { it != DEFAULT_VERSION } ?: "1.0.0-SNAPSHOT"
description = "Generate Build.java for module"

repositories {
    mavenLocal()
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    kapt("com.google.auto.service:auto-service:1.0")
    implementation(gradleApi())
    implementation(kotlin("bom"))
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

gradlePlugin {
    plugins {
        create("buildPropsPlugin") {
            id = "io.johnsonlee.buildprops"
            implementationClass = "io.johnsonlee.buildprops.BuildPropsPlugin"
        }
    }
}
