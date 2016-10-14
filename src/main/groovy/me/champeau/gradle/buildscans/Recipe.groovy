package me.champeau.gradle.buildscans

import com.gradle.scan.plugin.BuildScanExtension
import org.gradle.api.invocation.Gradle

interface Recipe {
    void apply(Gradle gradle, BuildScanExtension buildScan)
}