import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.errors.RepositoryNotFoundException


def git

try {
    git = Grgit.open(currentDir: gradle.rootProject.projectDir)
} catch(RepositoryNotFoundException e) {
    // ignore
}

buildScan.with {
    def commitId = git.head().id
    value 'Git Commit ID', commitId
    if (params.baseUrl) {
        link 'Sources', "${params.baseUrl}/$commitId"
    }

    def branchName = git.branch.current.name
    tag branchName
    value 'Git Branch Name', branchName

    def status = git.status()
    if (!status.isClean()) {
        tag 'dirty'
        def changes = []
        ['added', 'modified', 'removed'].each { change ->
            def files = [status.staged, status.unstaged].collect {
                it."$change"
            }.flatten().join(', ')
            if (files) {
                changes.add("$change: $files")
            }
        }
        value 'Git Status', changes.join('\n')
    }
}
