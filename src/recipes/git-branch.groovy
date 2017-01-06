try {
  def branchName = 'git rev-parse --abbrev-ref HEAD'.execute().text.trim()
  if(branchName && branchName != 'HEAD') {
    buildScan.tag branchName
    buildScan.value "Git Branch Name", branchName
  }
} catch(ignore) {
  // ignore
}
