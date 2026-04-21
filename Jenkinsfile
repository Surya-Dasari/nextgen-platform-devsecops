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

                    // -------- SONAR --------
                    def sonarRaw = sh(
                        script: '''
                        aws secretsmanager get-secret-value \
                          --secret-id dev/sonar/token \
                          --region ap-south-1 \
                          --query SecretString \
                          --output text
                        ''',
                        returnStdout: true
                    ).trim()

                    def sonarJson = readJSON text: sonarRaw
                    if (sonarJson instanceof String) {
                        sonarJson = readJSON text: sonarJson
                    }

                    if (!sonarJson.token) {
                        error "Sonar token missing from AWS secret"
                    }

                    env.SONAR_TOKEN = "${sonarJson.token}"

                    // -------- NEXUS --------
                    def nexusRaw = sh(
                        script: '''
                        aws secretsmanager get-secret-value \
                          --secret-id dev/nexus/creds \
                          --region ap-south-1 \
                          --query SecretString \
                          --output text
                        ''',
                        returnStdout: true
                    ).trim()

                    def nexusJson = readJSON text: nexusRaw
                    if (nexusJson instanceof String) {
                        nexusJson = readJSON text: nexusJson
                    }

                    if (!nexusJson.username || !nexusJson.password) {
                        error "Nexus credentials missing from AWS secret"
                    }

                    env.NEXUS_USER = "${nexusJson.username}"
                    env.NEXUS_PASS = "${nexusJson.password}"
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

    stage('Quality Gate (Manual)') {
        steps {
            script {
                sleep 10

                for (svc in ['apiservice','authservice','userservice']) {
                    def status = sh(
                        script: """
                        curl -s -u $SONAR_TOKEN: \
                        "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=nextgen-${svc}" \
                        | jq -r .projectStatus.status
                        """,
                        returnStdout: true
                    ).trim()

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

            SETTINGS="\$WORKSPACE/settings.xml"

            cat > "\$SETTINGS" <<EOF


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


            for svc in apiservice authservice userservice
            do
              cd services/\$svc
              mvn clean deploy -s "\$SETTINGS" -DskipTests
              cd -
            done
            """
        }
    }

    stage('Build Frontend') {
        steps {
            sh '''
            cd services/frontend
            npm install
            '''
        }
    }
}

stage('Build Docker Images') {
    steps {
        sh '''
        set -e

        for svc in apiservice authservice userservice
        do
          echo "Building image for $svc"
          cd services/$svc

          docker build -t nextgen-$svc:latest .

          cd -
        done

        echo "Building frontend image"
        cd services/frontend
        docker build -t nextgen-frontend:latest .
        cd -
        '''
    }
}

stage('Trivy Scan') {
    steps {
        sh '''
        set -e

        for img in apiservice authservice userservice frontend
        do
          echo "Scanning image nextgen-$img"

          trivy image \
            --severity HIGH,CRITICAL \
            --exit-code 1 \
            nextgen-$img:latest
        done
        '''
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
