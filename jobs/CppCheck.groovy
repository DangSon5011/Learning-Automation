/*
* Copyright: Son Dang Van
*
*/

pipeline {
    agent {
        label 'built-in'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '60', artifactNumToKeepStr: '60'))
        timeout(time: 10, unit: 'MINUTES')
        skipDefaultCheckout()
        timestamps()
    }
    parameters {
        string(name: 'GIT_REPO', description: 'description', defaultValue: 'https://github.com/DangSon5011/Cplusplus-jenkins.git')
        string(name: 'GIT_BRANCH', description: 'branch checkout', defaultValue: 'main')
    }
    stages {
        stage('Init') {
            steps {
                echo 'CppCheck on pull-request'
            }
        }
        stage('Checkout') {
            steps {
                git branch: params.GIT_BRANCH, url: params.GIT_REPO
                // checkout([$class: 'GitSCM', branches: [[name: '*/main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/DangSon5011/Cplusplus-jenkins.git']]])
            }
        }
        // stage('Run') {
        // }
    }
}