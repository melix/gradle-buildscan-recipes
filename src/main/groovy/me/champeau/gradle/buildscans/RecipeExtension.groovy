/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.champeau.gradle.buildscans

import com.gradle.scan.plugin.BuildScanExtension
import groovy.transform.CompileStatic
import org.gradle.api.invocation.Gradle

@CompileStatic
class RecipeExtension {
    private final Recipes recipes

    RecipeExtension(BuildScanExtension bse, Gradle gradle) {
        this.recipes = new Recipes(bse, gradle)
    }

    void recipe(Map<String, String> params = [:], String recipeName) {
        recipes.apply(recipeName, params)
    }

    void recipes(String... recipeList) {
        recipeList.each { String recipe ->
            recipes.apply(recipe)
        }
    }
}
