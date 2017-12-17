def env = System.getenv()

if (!(env.CI && env.GITLAB_CI)) {
    return
}

buildScan.with {
    tag 'CI'
    link 'GitLab CI Job', "${env.CI_PROJECT_URL}/-/jobs/${env.CI_JOB_ID}"
    link 'GitLab CI Pipeline', "${env.CI_PROJECT_URL}/pipelines/${env.CI_PIPELINE_ID}"

    value 'GitLab CI Project Name', env.CI_PROJECT_NAME
    value 'GitLab CI Project Namespace', env.CI_PROJECT_NAMESPACE
    value 'GitLab CI Project Path', env.CI_PROJECT_PATH
    value 'GitLab CI Project URL', env.CI_PROJECT_URL
    value 'GitLab CI Built Branch', env.CI_COMMIT_REF_NAME
    value 'GitLab CI Built Commit', env.CI_COMMIT_SHA
    if (env.CI_COMMIT_TAG) {
        value 'GitLab CI Tag', env.CI_COMMIT_TAG
    }
    value 'GitLab CI Job Started Manually', env.CI_JOB_MANUAL
    value 'GitLab CI Job Name', env.CI_JOB_NAME
    value 'GitLab CI Job Stage', env.CI_JOB_STAGE
    value 'GitLab CI Pipeline Source', env.CI_PIPELINE_SOURCE
    value 'GitLab CI Environment', env.CI_ENVIRONMENT_NAME
    value 'GitLab CI Environment URL', env.CI_ENVIRONMENT_URL
    value 'GitLab CI Runner ID', env.CI_RUNNER_ID
    value 'GitLab CI Runner Desc', env.CI_RUNNER_DESCRIPTION
    value 'GitLab CI Runner Tags', env.CI_RUNNER_TAGS
    value 'GitLab CI Server Name', env.CI_SERVER_NAME
    value 'GitLab CI Server Revision', env.CI_SERVER_REVISION
    value 'GitLab CI Server Version', env.CI_SERVER_VERSION
    value 'GitLab CI User ID', env.GITLAB_USER_ID
    value 'GitLab CI User Email', env.GITLAB_USER_EMAIL
    value 'GitLab CI User Login', env.GITLAB_USER_LOGIN
    value 'GitLab CI User Name', env.GITLAB_USER_NAME
}
