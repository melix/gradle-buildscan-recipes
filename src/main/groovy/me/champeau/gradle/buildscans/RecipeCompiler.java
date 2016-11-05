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
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.invocation.Gradle;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RecipeCompiler extends CompilationCustomizer {
    private static final byte VERSION = 1;

    private static final ClassNode BUILDSCAN_TYPE = ClassHelper.make(BuildScanExtension.class);
    private static final ClassNode GRADLE_TYPE = ClassHelper.make(Gradle.class);
    private static final ClassNode MAP_TYPE = ClassHelper.make(Map.class);

    public RecipeCompiler() {
        super(CompilePhase.CONVERSION);
    }

    @Override
    public void call(final SourceUnit sourceUnit, final GeneratorContext generatorContext, final ClassNode classNode) throws CompilationFailedException {
        if (classNode.isScript()) {
            classNode.setName(recipeClassName(classNode.getName()));
            classNode.addInterface(ClassHelper.make(Recipe.class));
            classNode.setSuperClass(ClassHelper.OBJECT_TYPE);
            MethodNode run = classNode.getMethods("run").get(0);
            classNode.getMethods().remove(run);
            ClassNode mapType = MAP_TYPE.getPlainNodeReference();
            mapType.setGenericsTypes(new GenericsType[]{
                    new GenericsType(ClassHelper.STRING_TYPE),
                    new GenericsType(ClassHelper.STRING_TYPE),
            });
            MethodNode apply = new MethodNode("apply",
                    Modifier.PUBLIC,
                    ClassHelper.VOID_TYPE,
                    new Parameter[]{
                            new Parameter(GRADLE_TYPE, "gradle"),
                            new Parameter(BUILDSCAN_TYPE, "buildScan"),
                            new Parameter(mapType, "params"),
                    },
                    ClassNode.EMPTY_ARRAY,
                    run.getCode());
            classNode.addMethod(apply);
        }
    }

    public static String recipeClassName(String recipe) {
        return "me.champeau.gradle.buildscans.GeneratedRecipe_" + recipe.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public static Class<? extends Recipe> compileOrGetFromCache(Gradle gradle, String recipeName, URL url, boolean cache) throws NoSuchAlgorithmException,
            URISyntaxException, IOException, ClassNotFoundException {
        File recipeDir = recipeDir(gradle, url);
        if (!cache && recipeDir.exists()) {
            // cleanup
            ResourceGroovyMethods.deleteDir(recipeDir);
        }
        if (!cache || recipeDir.mkdir()) {
            long sd = System.nanoTime();
            try {
                return compileToDir(recipeName, url, recipeDir, cache);
            } finally {
                gradle.getRootProject().getLogger().debug("Compilation took " + (TimeUnit.MILLISECONDS.convert(System.nanoTime()-sd, TimeUnit.NANOSECONDS)) + "ms");
            }
        }
        return loadFromDir(recipeName, recipeDir);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Recipe> loadFromDir(final String recipeName, final File recipeDir) throws MalformedURLException, ClassNotFoundException {
        URLClassLoader ucl = new URLClassLoader(new URL[] {recipeDir.toURI().toURL()}, RecipeCompiler.class.getClassLoader());
        return (Class<? extends Recipe>) ucl.loadClass(recipeClassName(recipeName));
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Recipe> compileToDir(final String recipeName, final URL url, final File recipeDir, final boolean cache) throws IOException {
        System.out.println("Compiling recipe " + recipeName +"...");
        CompilerConfiguration config = new CompilerConfiguration();
        if (cache) {
            config.setTargetDirectory(recipeDir);
        }
        config.addCompilationCustomizers(new RecipeCompiler());
        GroovyClassLoader gcl = new GroovyClassLoader(RecipeCompiler.class.getClassLoader(), config);
        return (Class<? extends Recipe>) gcl.parseClass(ResourceGroovyMethods.getText(url, "UTF-8"), recipeName + ".groovy");
    }

    private static File recipeDir(final Gradle gradle, final URL url) throws NoSuchAlgorithmException, UnsupportedEncodingException, URISyntaxException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        digest.update(url.toURI().toString().getBytes("utf-8"));
        digest.update(VERSION);
        File gradleUserHomeDir = gradle.getGradleUserHomeDir();
        File recipesHomeDir = new File(gradleUserHomeDir, "buildScanRecipes");
        if (!recipesHomeDir.exists()) {
            recipesHomeDir.mkdirs();
        }
        String hex = new BigInteger(1, digest.digest()).toString(16);
        return new File(recipesHomeDir, hex);
    }
}
