package io.johnsonlee.buildprops

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import java.io.File

/**
 * @author johnsonlee
 */
class BuildPropsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("idea")) {
            project.plugins.apply("idea")
        }

        val convention = project.convention
        val javaPlugin = convention.getPlugin(JavaPluginConvention::class.java)
        val sourceSets = javaPlugin.sourceSets
        val compileTasks = sourceSets.filter {
            it.name == SourceSet.MAIN_SOURCE_SET_NAME
        }.map { sourceSet ->
            listOf("java", "kotlin", "groovy").map { lang ->
                sourceSet.getCompileTaskName(lang)
            }
        }.flatten().mapNotNull { taskName ->
            try {
                project.tasks.named(taskName)
            } catch (_: UnknownTaskException) {
                null
            }
        }

        project.configureIdeaModule(sourceSets)
        project.tasks.register("generateBuildJavaFile", BuildGenerator::class.java) { buildProps ->
            compileTasks.forEach { compile ->
                compile.configure {
                    (it.dependsOn(buildProps) as? SourceTask)?.source(buildProps.output)
                }
            }
        }
    }

}

internal const val PLUGIN_ID = "buildprops"

internal val GENERATED_SOURCE_ROOT = "generated${File.separator}source${File.separator}$PLUGIN_ID"
