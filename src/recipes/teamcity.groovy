def env = System.getenv()

if (env.TEAMCITY_VERSION) {
    def version = env.TEAMCITY_VERSION
    buildScan.tag 'CI'
    def guest = params.guest?'&guest=1':''
    def buildId = System.getProperty('teamcity.agent.dotnet.build_id', null)
    if (env.BUILD_URL) {
        buildScan.link "TeamCity $version build", env.BUILD_URL
    } else if (params.baseUrl && buildId) {
        buildScan.link "TeamCity $version build", "${params.baseUrl}/viewLog.html?buildId=$buildId$guest"
    }
    buildScan.value "Teamcity $version build name", env.TEAMCITY_BUILDCONF_NAME

}

