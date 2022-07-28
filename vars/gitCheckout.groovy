#!/usr/bin/env groovy
/*
 * Copyright 2021 NXP
 */

/**
 * Wrapper around GitSCM checkout step.
 *
 * @param url                   repository URL
 * @param branch                branch name to checkout
 * @param cleanBeforeCheckout   clean before checking out (default: false)
 * @param remoteName            remote name (default: origin)
 * @param shallow               perform a shallow clone (default: false)
 * @param rebaseOnto            perform a rebase onto the branch specified after checkout (default: null)
 * @param resetToCommit         reset HEAD to specific commit after checkout (default: null)
 * @param credentialsId         credentials to use when cloning (default: 'zephyr-ciservice-ssh')
 *
 * Examples:
 * gitCheckout(url: 'ssh://git@bitbucket.sw.nxp.com/az/zephyr.git', branch: 'develop', cleanBeforeCheckout: true)
 */
def call(Map args) {
    if (!args.url) {
        error 'missing argument "url"'
    } else if (!args.branch) {
        error 'missing argument "branch"'
    }

    String remoteName = args.remoteName ?: 'origin'
    List extensions = [
        [$class: 'PruneStaleBranch'],
        [$class: 'LocalBranch', localBranch: args.branch],
    ]

    if (args.cleanBeforeCheckout) {
        extensions += [$class: 'CleanBeforeCheckout']
    }

    // if (args.shallow) {
    //     extensions += [$class: 'CloneOption', depth: 1, honorRefspec: true, noTags: false, reference: '', shallow: true]
    // }

    def scmVars = checkout(
        poll: false,
        scm: [$class: 'GitSCM',
             branches: [[name: args.branch]],
             userRemoteConfigs: [[
                 credentialsId: args.credentialsId ?: 'zephyr-ciservice-ssh',
                 url: args.url,
                 name: remoteName
             ]],
             doGenerateSubmoduleConfigurations: false,
             submoduleCfg: [],
             extensions: extensions
    ])

    if (args.resetToCommit) {
        sh "git reset ${args.resetToCommit} --hard"
    }

    /* perform rebase once checked out using plain Git commands, since GitSCM doesn't support this operation */
    if (args.rebaseOnto) {
        sh """
        git config user.email "jenkins@jenkins.com"
        git config user.name "jenkins"
        git branch -D ${args.rebaseOnto} || true
        git fetch ${remoteName} ${args.rebaseOnto}:${args.rebaseOnto}
        git rebase -Xignore-space-at-eol ${remoteName}/${args.rebaseOnto} || { git rebase --abort && false; }
        """
    }

    /* for declarative pipelines is needed to manually inject these variables into env */
    env.GIT_COMMIT = args.resetToCommit ? args.resetToCommit : scmVars.get('GIT_COMMIT')
    env.GIT_BRANCH = scmVars.get('GIT_BRANCH')
    env.GIT_URL = scmVars.get('GIT_URL')
    env.getEnvironment().each { key, value -> if (key.startsWith('GIT_')) echo "$key = $value" }

    sh 'git log -10 --pretty=oneline --abbrev-commit'
}
