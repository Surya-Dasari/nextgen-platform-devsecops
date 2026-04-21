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

    stage('Fetch AWS Secrets') {
        steps {
            withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
                script {
                    // Sonar Token
                    def sonarSecret = sh(
                        script: '''
                        aws secretsmanager get-secret-value \
                          --secret-id dev/sonar/token \
                          --region ap-south-1 \
                          --query SecretString \
                          --output text
                        ''',
                        returnStdout: true
                    ).trim()

                    def sonarJson = readJSON text: sonarSecret
                    env.SONAR_TOKEN = sonarJson.token

                    // Nexus Credentials
                    def nexusSecret = sh(
                        script: '''
                        aws secretsmanager get-secret-value \
                          --secret-id dev/nexus/creds \
                          --region ap-south-1 \
                          --query SecretString \
                          --output text
                        ''',
                        returnStdout: true
                    ).trim()

                    def nexusJson = readJSON text: nexusSecret
                    env.NEXUS_USER = nexusJson.username
                    env.NEXUS_PASS = nexusJson.password
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

    stage('Quality Gate (Manual)') {
        steps {
            script {
                sleep(time: 10, unit: 'SECONDS')

                for (svc in ['apiservice', 'authservice', 'userservice']) {

                    echo "Checking Quality Gate for ${svc}"

                    def status = sh(
                        script: """
                        curl -s -u $SONAR_TOKEN: \
                        "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=nextgen-${svc}" \
                        | jq -r .projectStatus.status
                        """,
                        returnStdout: true
                    ).trim()

                    echo "Quality Gate Status for ${svc}: ${status}"

                    if (status != "OK") {
                        error "Quality Gate failed for ${svc}"
                    }
                }
            }
        }
    }

    stage('Publish to Nexus') {
        steps {
            sh """
            set -e

            echo "Creating Maven settings.xml"

            cat > settings.xml <<EOF


<settings>
  <servers>
    <server>
      <id>nexus-releases</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
    <server>
      <id>nexus-snapshots</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
EOF


            echo "Publishing artifacts to Nexus..."

            for svc in apiservice authservice userservice
            do
              sh 'echo "---- settings.xml ----"'
              sh 'cat settings.xml'
              echo "Deploying \$svc"
              cd services/\$svc
              mvn clean deploy -s ../../settings.xml -DskipTests -x
              cd -
            done
            """
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
