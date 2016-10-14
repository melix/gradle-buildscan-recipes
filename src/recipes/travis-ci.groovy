def env = System.getenv()

if (env.CI && env.TRAVIS && env.BUILD_SERVER_URL && env.BUILD_TYPE_ID) {
    buildScan.tag 'CI'
    buildScan.value 'Travis Branch', env.TRAVIS_BRANCH
    buildScan.value 'Travis Commit', env.TRAVIS_COMMIT
    buildScan.value 'Travis Pull Request', env.TRAVIS_PULL_REQUEST
    if (env.TRAVIS_TAG) {
        buildScan.value 'Travis Tag', env.TRAVIS_TAG
    }
}
