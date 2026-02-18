pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {
        SONAR_SERVER = 'SonarServer'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Validate Release Tag') {
            steps {
                sh 'python3 scripts/validate_release.py'
            }
        }

        stage('Set Version') {
            steps {
                sh './scripts/set_version.sh'
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('Sonar Scan') {
            steps {
                withSonarQubeEnv("${SONAR_SERVER}") {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 3, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy Artifact') {
            steps {
                sh 'mvn deploy -DskipTests'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}

