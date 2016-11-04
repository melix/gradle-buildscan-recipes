/**
 * A meta-recipe which will download another recipe from Gist.
 *
 * This recipe will fetch a Gist, compile it as a recipe, and execute it. It accepts the following parameters:
 *
 * @param user username of the gist owner
 * @param id id of the gist
 * @param recipe name of the file containing the gist
 * @param rev revision of the gist. If absent, the compiled recipe will not be cached.
 *
 * Any additional parameter will be passed to the remote recipe.
 *
 * Example usage. For the gist at:
 *   the name of the recipe is: my-recipe (my-recipe.groovy)
 *   the id is 5944cb701d6c9650ecaccccd4642ea5f
 *   and the revision can be found when clicking on "raw": 4b40b45559929ee2baaa7599e29dd78e51c3843a
 *
 *   https://gist.githubusercontent.com/melix/5944cb701d6c9650ecaccccd4642ea5f/raw/4b40b45559929ee2baaa7599e29dd78e51c3843a/my-recipe.groovy
 *
 *  recipe 'gist',
 *         user: 'melix',
 *         id: '5944cb701d6c9650ecaccccd4642ea5f',
 *         rev: '4b40b45559929ee2baaa7599e29dd78e51c3843a',
 *         recipe: 'my-recipe',
 *         // remote recipe attributes
 *         name: 'Bob'
 */

import me.champeau.gradle.buildscans.RecipeCompiler

def user = params.user
def id = params.id
def rev = params.rev
def recipe = params.recipe

boolean cache = rev

if (!cache) {
    println "Warning, recipe $recipe will not be cached because revision isn't set"
}

def url = "https://gist.githubusercontent.com/$user/$id/raw/$rev/${recipe}.groovy"

def recipeClass = RecipeCompiler.compileOrGetFromCache(gradle, recipe, new URL(url), cache)
recipeClass.newInstance().apply(gradle, buildScan, params)
