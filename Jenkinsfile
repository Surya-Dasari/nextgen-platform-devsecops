pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {
        SONAR_SERVER = 'SonarServer'
        DOCKER_REGISTRY = 'suryadasari31'
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
                sh 'chmod +x scripts/set_version.sh'
                sh './scripts/set_version.sh'
            }
        }

        stage('Read Version & Git SHA') {
            steps {
                script {
                    env.APP_VERSION = sh(
                        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                        returnStdout: true
                    ).trim()

                    env.GIT_SHA = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()

                    echo "Version: ${APP_VERSION}"
                    echo "Git SHA: ${GIT_SHA}"
                }
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn clean verify -T 1C'
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
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy To Nexus') {
            steps {
                sh 'mvn deploy -DskipTests'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = ["authservice", "userservice", "apiservice", "frontend"]

                    for (svc in services) {
                        sh """
                        docker build -t ${DOCKER_REGISTRY}/${svc}:${APP_VERSION} \
                                     -t ${DOCKER_REGISTRY}/${svc}:${GIT_SHA} \
                                     services/${svc}
                        """
                    }
                }
            }
        }

        stage('Trivy Image Scan') {
            steps {
                script {
                    def services = ["authservice", "userservice", "apiservice", "frontend"]

                    for (svc in services) {
                        sh """
                        trivy image --scanners vuln --exit-code 1 --severity CRITICAL \
                        ${DOCKER_REGISTRY}/${svc}:${APP_VERSION}
                        """
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {

                    sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"

                    script {
                        def services = ["authservice", "userservice", "apiservice", "frontend"]

                        for (svc in services) {
                            sh "docker push ${DOCKER_REGISTRY}/${svc}:${APP_VERSION}"
                            sh "docker push ${DOCKER_REGISTRY}/${svc}:${GIT_SHA}"
                        }

                        if (env.BRANCH_NAME == "dev") {
                            for (svc in services) {
                                sh """
                                docker tag ${DOCKER_REGISTRY}/${svc}:${APP_VERSION} \
                                           ${DOCKER_REGISTRY}/${svc}:latest
                                docker push ${DOCKER_REGISTRY}/${svc}:latest
                                """
                            }
                        }
                    }

                    sh "docker logout"
                }
            }
        }

        // 🔥 NEW STAGE – GitOps Image Update
        stage('Update Kubernetes Manifests (GitOps)') {
            when {
                branch 'dev'
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'github-creds',
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_TOKEN'
                )]) {

                    sh """
                    set -e

                    git config user.email "ci@jenkins"
                    git config user.name "jenkins"

                    sed -i 's|image: suryadasari31/apiservice:.*|image: suryadasari31/apiservice:${APP_VERSION}|' deploy/base/apiservice-deployment.yaml
                    sed -i 's|image: suryadasari31/authservice:.*|image: suryadasari31/authservice:${APP_VERSION}|' deploy/base/authservice-deployment.yaml
                    sed -i 's|image: suryadasari31/userservice:.*|image: suryadasari31/userservice:${APP_VERSION}|' deploy/base/userservice-deployment.yaml
                    sed -i 's|image: suryadasari31/frontend:.*|image: suryadasari31/frontend:${APP_VERSION}|' deploy/base/frontend-deployment.yaml

                    git add deploy/base/*.yaml
                    git commit -m "Update image tags to ${APP_VERSION}" || echo "No changes to commit"

                    git push https://\$GIT_USER:\$GIT_TOKEN@github.com/Surya-Dasari/nextgen-platform-devsecops.git HEAD:dev
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Secure CI → GitOps → ArgoCD completed for ${APP_VERSION}"
        }
        failure {
            echo "Pipeline failed due to security or build errors."
        }
    }
}
