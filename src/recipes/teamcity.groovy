def env = System.getenv()

if (env.CI && env.BUILD_ID && env.BUILD_SERVER_URL && env.BUILD_TYPE_ID && env.BUILD_URL) {
    buildScan.tag 'CI'
    buildScan.link "TeamCity Build", env.BUILD_URL
    buildScan.value "Build ID", env.BUILD_ID
}

