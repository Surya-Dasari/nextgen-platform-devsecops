pipeline {
    agent any

    environment {
        DOCKER_REPO = "docker.io/suryadasari31"
        IMAGE_TAG   = "${BUILD_NUMBER}"
        OCP_SERVER  = "https://api.rm2.thpm.p1.openshiftapps.com:6443"
        OCP_PROJECT = "suryadasari31-dev"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                sh '''
                set -e
                for svc in apiservice authservice userservice
                do
                  echo "🔧 Building $svc"
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
                npm run build || true
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                set -e
                for svc in apiservice authservice userservice frontend
                do
                  echo "🐳 Building image for $svc"
                  docker build -t $DOCKER_REPO/nextgen-$svc:$IMAGE_TAG services/$svc
                done
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    set -e
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    for svc in apiservice authservice userservice frontend
                    do
                      docker push $DOCKER_REPO/nextgen-$svc:$IMAGE_TAG
                    done
                    '''
                }
            }
        }

        stage('Prepare Secrets (Idempotent)') {
            steps {
                withCredentials([
                    string(credentialsId: 'openshift-token', variable: 'OCP_TOKEN'),
                    string(credentialsId: 'pg-db-user', variable: 'DB_USER'),
                    string(credentialsId: 'pg-db-password', variable: 'DB_PASS'),
                    string(credentialsId: 'pg-db-name', variable: 'DB_NAME')
                ]) {
                    sh '''
                    set -e
                    oc login --token=$OCP_TOKEN --server=$OCP_SERVER --insecure-skip-tls-verify=true
                    oc project $OCP_PROJECT

                    oc get secret postgres-secret || \
                    oc create secret generic postgres-secret \
                      --from-literal=POSTGRES_USER=$DB_USER \
                      --from-literal=POSTGRES_PASSWORD=$DB_PASS \
                      --from-literal=POSTGRES_DB=$DB_NAME

                    oc get secret userservice-db-secret || \
                    oc create secret generic userservice-db-secret \
                      --from-literal=SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/$DB_NAME \
                      --from-literal=SPRING_DATASOURCE_USERNAME=$DB_USER \
                      --from-literal=SPRING_DATASOURCE_PASSWORD=$DB_PASS
                    '''
                }
            }
        }

        stage('Prepare Scripts') {
    steps {
        sh '''
        chmod +x scripts/*.sh
        '''
    }
}


        stage('Deploy to OpenShift') {
            steps {
                withCredentials([string(
                    credentialsId: 'openshift-token',
                    variable: 'OCP_TOKEN'
                )]) {
                    sh '''
                    set -e
                    oc login --token=$OCP_TOKEN --server=$OCP_SERVER --insecure-skip-tls-verify=true
                    oc project $OCP_PROJECT

                    oc apply -f services/postgres/

                    ./scripts/render-manifest.sh services/apiservice/openshift.yaml $IMAGE_TAG | oc apply -f -
                    ./scripts/render-manifest.sh services/authservice/openshift.yaml $IMAGE_TAG | oc apply -f -
                    ./scripts/render-manifest.sh services/userservice/openshift.yaml $IMAGE_TAG | oc apply -f -
                    ./scripts/render-manifest.sh services/frontend/openshift.yaml $IMAGE_TAG | oc apply -f -
                    '''
                }
            }
        }

        stage('Verify Rollout') {
            steps {
                sh '''
                python3 scripts/verify-rollout.py
                '''
            }
        }
    }

    post {
        success {
            echo "✅ CI/CD SUCCESS – immutable rollout completed"
        }
        failure {
            echo "❌ CI/CD FAILED – check rollout or logs"
        }
    }
}

