package io.johnsonlee.buildprops

import org.gradle.api.internal.AbstractTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.StringTokenizer

/**
 * @author johnsonlee
 */
open class BuildGenerator : AbstractTask() {

    @get:OutputDirectory
    val output: File = project.getGeneratedSourceDir(project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME))

    @TaskAction
    fun generate() {
        val pkg = mkpkg("${project.group}.${project.name}")
        val path = "${pkg.replace(".", File.separator)}${File.separator}Build.java"
        val revision = File(project.rootProject.projectDir, GIT_LOG_HEAD).takeIf {
            it.exists() && it.canRead() && it.length() > 0
        }?.useLines {
            StringTokenizer(it.last()).nextToken(1)
        } ?: ""

        File(output, path).also {
            it.parentFile.mkdirs()
            it.createNewFile()
        }.printWriter().use {
            it.apply {
                it.println("""
                /**
                 * DO NOT MODIFY! This file is generated automatically.
                 */
                package $pkg;
                
                public interface Build {
                    String GROUP = "${project.group}";
                    String ARTIFACT = "${project.name}";
                    String VERSION = "${project.version}";
                    String REVISION = "$revision";
                }
                """.trimIndent())
            }
        }
    }

}

private val GIT_LOG_HEAD = arrayOf(".git", "logs", "HEAD").joinToString(File.separator)

/**
 * Make a safe package name
 */
internal fun mkpkg(s: String): String {
    return StringBuilder(s.length).apply {
        s.forEachIndexed { i, c ->
            if (0 == i) {
                append(if ('.' != c && !c.isJavaIdentifierStart()) '.' else c)
            } else {
                append(if ('.' != c && !c.isJavaIdentifierPart()) '.' else c)
            }
        }
    }.toString().split('.').let {
        it.filterIndexed { i, s -> !(i > 0 && s == it[i - 1]) }
    }.joinToString(".")
}

internal fun StringTokenizer.nextToken(nth: Int): String? {
    var i = 0

    while (hasMoreTokens()) {
        val token = nextToken()
        if (nth == i++) {
            return token
        }
    }

    return null
}
