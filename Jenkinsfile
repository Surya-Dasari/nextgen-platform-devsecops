pipeline {
agent any

options {
    timestamps()
}

tools {
    jdk 'jdk17'
    nodejs 'node18'
}

stages {

    stage('Checkout') {
        steps {
            checkout scm
        }
    }

    stage('Gitleaks Scan') {
        steps {
            sh '''
            echo "Running Gitleaks scan..."
            gitleaks detect --source . --config .gitleaks.toml --no-banner
            '''
        }
    }

    stage('Build Backend') {
        steps {
            sh '''
            set -e
            for svc in apiservice authservice userservice
            do
              echo "Building $svc"
              cd services/$svc
              mvn clean package -DskipTests
              cd -
            done
            '''
        }
    }

    stage('Fetch Sonar Token') {
        steps {
            withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
                script {
                    def secret = sh(
                        script: '''
                        aws secretsmanager get-secret-value \
                          --secret-id dev/sonar/token \
                          --region ap-south-1 \
                          --query SecretString \
                          --output text
                        ''',
                        returnStdout: true
                    ).trim()

                    def json = readJSON text: secret
                    env.SONAR_TOKEN = json.token
                }
            }
        }
    }

    stage('SonarQube Analysis') {
        steps {
            withSonarQubeEnv('sonar') {
                sh '''
                set -e
                for svc in apiservice authservice userservice
                do
                  echo "Running Sonar for $svc"
                  cd services/$svc
                  mvn sonar:sonar \
                    -Dsonar.projectKey=nextgen-$svc \
                    -Dsonar.host.url=$SONAR_HOST_URL \
                    -Dsonar.login=$SONAR_TOKEN
                  cd -
                done
                '''
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

    stage('Build Frontend') {
        steps {
            sh '''
            set -e
            cd services/frontend
            npm install
            '''
        }
    }
}

post {
    success {
        echo "CI completed successfully"
    }
    failure {
        echo "CI failed"
    }
}

}

