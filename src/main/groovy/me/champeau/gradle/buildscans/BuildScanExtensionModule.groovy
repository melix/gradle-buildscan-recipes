package me.champeau.gradle.buildscans

import groovy.transform.CompileStatic
import com.gradle.scan.plugin.BuildScanExtension

@CompileStatic
class BuildScanExtensionModule {
   static void recipe(BuildScanExtension self, String recipe) { println "Recipe: $recipe" }
}

