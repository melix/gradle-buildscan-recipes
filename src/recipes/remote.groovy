/**
 * A meta-recipe which will download another recipe from any remote URL.
 *
 * This recipe will fetch a remote URL, compile it as a recipe, and execute it. It accepts the following parameters:
 *
 * @param url URL of the recipe. Must be a .groovy file.
 * @param cache cache the compiled script, avoiding to fetch it again next time ("true" or "false")
 *
 * Any additional parameter will be passed to the remote recipe.
 *
 * Example usage:
 *  recipe 'remote',
 *         url: 'https://gist.githubusercontent.com/melix/5944cb701d6c9650ecaccccd4642ea5f/raw//my-recipe.groovy',
 *         cache: 'false',
 *         // remote recipe attributes
 *         name: 'Bob'
 */

import me.champeau.gradle.buildscans.RecipeCompiler

def url = new URL(params.url)
String recipe = (url.file - '.groovy').replaceAll('.+[/]', '')

boolean cache = Boolean.valueOf(params.rev?:'false')

def recipeClass = RecipeCompiler.compileOrGetFromCache(gradle, recipe, url, cache)
recipeClass.newInstance().apply(gradle, buildScan, params)
