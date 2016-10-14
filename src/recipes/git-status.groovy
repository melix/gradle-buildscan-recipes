try {
    def status = 'git status --porcelain'.execute().text
    if (status) {
        buildScan.tag "dirty"
        buildScan.value "Git Status", status
    }
} catch (ignore) {
    // ignore
}
