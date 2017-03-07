package me.champeau.gradle.buildscans

import com.gradle.scan.plugin.BuildScanExtension
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class RecipesPlugin implements Plugin<Project> {
    void apply(Project project) {
        def buildScanExtension = (BuildScanExtension) project.extensions.getByName('buildScan')
        project.extensions.create('buildScanRecipes', RecipeExtension, buildScanExtension, project.gradle)
    }
}
