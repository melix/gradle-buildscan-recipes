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
package me.champeau.gradle.buildscans;

import com.gradle.scan.plugin.BuildScanExtension;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.gradle.api.invocation.Gradle;

import java.lang.reflect.Modifier;

public class RecipeCompiler extends CompilationCustomizer {

    private static final ClassNode BUILDSCAN_TYPE = ClassHelper.make(BuildScanExtension.class);
    private static final ClassNode GRADLE_TYPE = ClassHelper.make(Gradle.class);

    public RecipeCompiler() {
        super(CompilePhase.CONVERSION);
    }

    @Override
    public void call(final SourceUnit sourceUnit, final GeneratorContext generatorContext, final ClassNode classNode) throws CompilationFailedException {
        classNode.setName(recipeClassName(classNode.getName()));
        classNode.addInterface(ClassHelper.make(Recipe.class));
        classNode.setSuperClass(ClassHelper.OBJECT_TYPE);
        MethodNode run = classNode.getMethods("run").get(0);
        classNode.getMethods().remove(run);
        MethodNode apply = new MethodNode("apply",
                Modifier.PUBLIC,
                ClassHelper.VOID_TYPE,
                new Parameter[]{
                        new Parameter(GRADLE_TYPE, "gradle"),
                        new Parameter(BUILDSCAN_TYPE, "buildScan"),
                },
                ClassNode.EMPTY_ARRAY,
                run.getCode());
        classNode.addMethod(apply);
    }

    public static String recipeClassName(String recipe) {
        return "me.champeau.gradle.buildscans.GeneratedRecipe_" + recipe.replaceAll("[^a-zA-Z0-9]", "_");
    }
}
