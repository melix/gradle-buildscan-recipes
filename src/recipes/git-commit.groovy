def commitId
try {
    commitId = 'git rev-parse --verify HEAD'.execute().text.trim()
} catch (ignore) {
    // ignore
}

if (commitId) {
    buildScan.value "Git Commit ID", commitId
}

