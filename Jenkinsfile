pipeline {
agent any

options {
timestamps()
}

tools {
jdk 'jdk17'
nodejs 'node18'
}

environment {
SONAR_HOST_URL = "http://172.25.233.203:9000"
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
          cd services/$svc
          mvn clean package -DskipTests
          cd -
        done
        '''
    }
}

stage('Fetch AWS Secrets') {
    steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
            script {
                def sonarRaw = sh(script: '''
                    aws secretsmanager get-secret-value \
                      --secret-id dev/sonar/token \
                      --region ap-south-1 \
                      --query SecretString \
                      --output text
                    ''', returnStdout: true).trim()

                def sonarJson = readJSON text: sonarRaw
                if (sonarJson instanceof String) {
                    sonarJson = readJSON text: sonarJson
                }

                env.SONAR_TOKEN = "${sonarJson.token}"
            }
        }
    }
}

stage('SonarQube Analysis') {
    steps {
        sh '''
        set -e
        for svc in apiservice authservice userservice
        do
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

stage('Build Docker Images') {
    steps {
        sh '''
        set -e

        for svc in apiservice authservice userservice
        do
          cd services/$svc
          docker build -t nextgen-$svc:${BUILD_NUMBER} .
          cd -
        done

        cd services/frontend
        docker build -t nextgen-frontend:${BUILD_NUMBER} .
        cd -
        '''
    }
}

stage('Push to Quay') {
    steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
            script {

                def quayRaw = sh(script: '''
                aws secretsmanager get-secret-value \
                  --secret-id dev/quay/creds \
                  --region ap-south-1 \
                  --query SecretString \
                  --output text
                ''', returnStdout: true).trim()

                def quayJson = readJSON text: quayRaw
                if (quayJson instanceof String) {
                    quayJson = readJSON text: quayJson
                }

                def QUAY_USER = quayJson.username
                def QUAY_PASS = quayJson.password

                sh """
                echo "$QUAY_PASS" | docker login quay.io -u "$QUAY_USER" --password-stdin

                for img in apiservice authservice userservice frontend
                do
                  docker tag nextgen-\$img:${BUILD_NUMBER} quay.io/suryadasari31/nextgen-\$img:${BUILD_NUMBER}
                  docker push quay.io/suryadasari31/nextgen-\$img:${BUILD_NUMBER}
                done
                """
            }
        }
    }
}

stage('Update GitOps Repo') {
    steps {
        sh '''
        set -e

        IMAGE_TAG=${BUILD_NUMBER}

        yq -i '.userservice.tag = "'"$IMAGE_TAG"'"' nextgen-platform/values.yaml
        yq -i '.authservice.tag = "'"$IMAGE_TAG"'"' nextgen-platform/values.yaml
        yq -i '.apiservice.tag = "'"$IMAGE_TAG"'"' nextgen-platform/values.yaml
        yq -i '.frontend.tag = "'"$IMAGE_TAG"'"' nextgen-platform/values.yaml

        git config user.name "jenkins"
        git config user.email "jenkins@local"

        git add nextgen-platform/values.yaml
        git commit -m "Update image tags to $IMAGE_TAG" || echo "No changes"
        git push origin eks-env
        '''
    }
}


}

post {
success {
echo "CI + GitOps trigger completed"
}
failure {
echo "Pipeline failed"
}
}
}
