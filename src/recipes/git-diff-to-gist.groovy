import com.gradle.scan.plugin.BuildScanExtension
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import org.gradle.api.Project

/**
 * This recipe will take the result of `git diff` and publish it to a Gist.
 * It requires authentication on GitHub, which can be achieved by creating a
 * personal access token. For security reasons, you should create a token
 * specifically for this recipe, and limited to "gist" access rights.
 *
 * @param public set to true if the Gist should be public. By default, false.
 * @param user your GitHub username. If not specified explicitly, will look for a project property named "gistUsername"
 * @param token your API token, created on https://github.com/settings/tokens If not specified explicitly, will look for a project property named "gistToken"
 * @param publish if you want to avoid publishing for each build, you can set this parameter to "false"
 *
 */

if (gradle.rootProject.findProperty('noGist') != null) {
    return
}

try {
    def diff = ['git', 'diff'].execute().text
    if (diff) {
        def baseUrl = new URL('https://api.github.com/gists')
        String credentials = "${params.user ?: gradle.rootProject.findProperty('gistUsername')}:${params.token ?: gradle.rootProject.findProperty('gistToken')}"
        String basicAuth = "Basic ${credentials.bytes.encodeBase64()}"
        try {
            HttpURLConnection connection = (HttpURLConnection) baseUrl.openConnection()
            connection.with {
                setRequestProperty("Authorization", basicAuth)
                doOutput = true
                requestMethod = 'POST'
                outputStream.withWriter { writer ->
                    jsonRequest(writer, gradle.rootProject, params.public ?: "false", diff)
                }
                createLink(content, buildScan)
            }
        } catch (ex) {
            gradle.rootProject.logger.warn("Unable to publish diff to Gist", ex)
        }
    }
} catch (ignore) {
    // ignore
}

@CompileDynamic
static void jsonRequest(Writer out, Project project, String isPublic, String diff) {
    project.findProperty()
    def builder = new JsonBuilder()
    builder {
        description("Git diff for $project.name")
        'public'(isPublic)
        files {
            "${project.name}.diff" {
                content diff
            }
        }
    }
    builder.writeTo(out)
}

@CompileDynamic
static void createLink(Object content, BuildScanExtension buildScan) {
    def parser = new JsonSlurper()
    def url = parser.parse(content.text.bytes).html_url
    buildScan.link('Git diff', url)
}