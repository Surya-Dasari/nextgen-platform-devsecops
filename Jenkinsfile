pipeline {
agent any

options {
    timestamps()
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
            gitleaks detect --source . --no-banner
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

    stage('Build Frontend') {
        steps {
            sh '''
            set -e
            cd services/frontend
            npm install
            npm run build
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

