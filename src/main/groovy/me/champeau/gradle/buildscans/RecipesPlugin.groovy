package me.champeau.gradle.buildscans

import com.gradle.scan.plugin.BuildScanExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class RecipesPlugin implements Plugin<Project> {
    void apply(Project project) {
        def extension = (BuildScanExtension) project.extensions.getByName('buildScan')
        def recipes = new Recipes(extension, project.gradle)
        // Would have loved to do it with Groovy extension module but they are not visible
        // from buildscripts even if the plugin is found on classpath
        // so we have to rely on dirty runtime metaprogramming tricks
        extension.metaClass.recipe = { String recipeName ->
            recipes.apply(recipeName)
        }
        extension.metaClass.recipes = { String... recipeList ->
            recipeList.each {
                recipes.apply(it)
            }
        }
    }
}
