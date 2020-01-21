package io.johnsonlee.buildprops

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaModule
import java.io.File

internal fun Project.getGeneratedSourceDir(sourceSet: SourceSet) = File(this.buildDir, GENERATED_SOURCE_ROOT + File.separator + sourceSet.name + File.separator + "java")

internal fun Project.configureIdeaModule(sourceSets: SourceSetContainer) {
    val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    val testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
    val mainGeneratedSourcesDir = getGeneratedSourceDir(mainSourceSet)
    val testGeneratedSourcesDir = getGeneratedSourceDir(testSourceSet)
    val ideaModule = extensions.getByType(IdeaModel::class.java).module
    ideaModule.excludeDirs = getIdeaExcludeDirs(getGeneratedSourceDirs(sourceSets), ideaModule)
    ideaModule.sourceDirs = files(ideaModule.sourceDirs, mainGeneratedSourcesDir).files
    ideaModule.testSourceDirs = files(ideaModule.testSourceDirs, testGeneratedSourcesDir).files
    ideaModule.generatedSourceDirs = files(ideaModule.generatedSourceDirs, mainGeneratedSourcesDir, testGeneratedSourcesDir).files
}

internal fun Project.getGeneratedSourceDirs(sourceSets: SourceSetContainer): Set<File> = LinkedHashSet<File>().also { excludes ->
    sourceSets.forEach { sourceSet ->
        var f = getGeneratedSourceDir(sourceSet)

        while (f != this.projectDir) {
            excludes.add(f)
            f = f.parentFile
        }
    }
}

internal fun Project.getIdeaExcludeDirs(excludes: Set<File>, ideaModule: IdeaModule): Set<File> = LinkedHashSet(ideaModule.excludeDirs).also { excludeDirs ->
    if (excludes.contains(buildDir) && excludeDirs.contains(buildDir)) {
        excludeDirs.remove(buildDir)
        buildDir.listFiles()?.filter {
            it.isDirectory
        }?.forEach {
            excludeDirs.add(it)
        }
    }

    excludeDirs.removeAll(excludes)
}