pipeline {
    agent any

    environment {
        APP_NAME = "nextgen-platform"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checking out source code..."
                checkout scm
            }
        }

        stage('Gitleaks Scan') {
            steps {
                echo "Running Gitleaks security scan..."
                sh '''
                    if command -v gitleaks >/dev/null 2>&1; then
                        gitleaks detect --source . --verbose
                    else
                        echo "Gitleaks not installed on Jenkins node"
                        exit 1
                    fi
                '''
            }
        }

        stage('Build Services') {
            parallel {

                stage('Build Userservice') {
                    steps {
                        dir('services/userservice') {
                            sh 'mvn clean package'
                        }
                    }
                }

                stage('Build Authservice') {
                    steps {
                        dir('services/authservice') {
                            sh 'mvn clean package'
                        }
                    }
                }

                stage('Build Apiservice') {
                    steps {
                        dir('services/apiservice') {
                            sh 'mvn clean package'
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully."
        }
        failure {
            echo "Pipeline failed."
        }
        always {
            cleanWs()
        }
    }
}

