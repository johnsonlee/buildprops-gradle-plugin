# Introduction

`buildprops-gradle-plugin` is used to generate `Build.java` source file with following constant fields:

- `GROUP_ID`
- `ARTIFACT_ID`
- `VERSION`
- `REVISION`

It not only support Java project, but also support Kotlin and Groovy project or mixin language project.

# Getting Started

Enable `buildprops-gradle-plugin` by configuring `build.gradle`

```kotlin
plugins {
    id("io.johnsonlee.buildprops") version "1.2.0"
}
```

Then build your porject to generate `Build.java` file, and then, the `Build` class will be accessible in your project

