import groovy.transform.CompileStatic

def cl = new URLClassLoader(new File('build/classpath.txt').text.split(':').collect {new File(it).toURI().toURL()} as URL[])

withConfig(configuration) {
    ast(CompileStatic)
}
configuration.addCompilationCustomizers(cl.loadClass('me.champeau.gradle.buildscans.RecipeCompiler').newInstance())
