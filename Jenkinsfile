pipeline {
    agent any

    environment {
        KUBECTL     = "/usr/local/bin/kubectl"
        HELM        = "/usr/local/bin/helm"
        DOCKER_REPO = "docker.io/suryadasari31"
        IMAGE_TAG   = "${BUILD_NUMBER}"
        K8S_CONTEXT = "kind-devops-lab"
        APP_NS      = "nextgen"
        MON_NS      = "monitoring"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'kind-cluster-dev',
                    url: 'https://github.com/Surya-Dasari/nextgen-platform-devsecops.git'
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
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                    for svc in apiservice authservice userservice frontend
                    do
                      docker push $DOCKER_REPO/nextgen-$svc:$IMAGE_TAG
                    done
                    '''
                }
            }
        }

        stage('Ensure Observability Stack') {
            steps {
                sh '''
                set -e

                $KUBECTL config use-context $K8S_CONTEXT

                $KUBECTL create namespace $MON_NS --dry-run=client -o yaml | $KUBECTL apply -f -

                $HELM repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
                $HELM repo add grafana https://grafana.github.io/helm-charts || true
                $HELM repo update

                $HELM upgrade --install monitoring prometheus-community/kube-prometheus-stack -n $MON_NS
                $HELM upgrade --install loki grafana/loki-stack -n $MON_NS --set grafana.enabled=false
                '''
            }
        }

        stage('Configure Slack Alerts') {
            steps {
                withCredentials([string(credentialsId: 'slack-webhook', variable: 'SLACK_WEBHOOK')]) {
                    sh '''
                    $KUBECTL create secret generic slack-webhook-secret \
                        --from-literal=slack_webhook=$SLACK_WEBHOOK \
                        -n $MON_NS \
                        --dry-run=client -o yaml | $KUBECTL apply -f -

                    $KUBECTL apply -f observability/alertmanager-config.yaml
                    '''
                }
            }
        }

        stage('Wait for Monitoring Stack') {
            steps {
                sh '''
                echo "Waiting for monitoring stack..."

                $KUBECTL wait --for=condition=Ready pod --all -n $MON_NS --timeout=300s

                $KUBECTL get pods -n $MON_NS
                '''
            }
        }

        stage('Prepare Scripts') {
            steps {
                sh 'chmod +x scripts/*.sh'
            }
        }

        stage('Deploy Application') {
            steps {
                sh '''
                $KUBECTL config use-context $K8S_CONTEXT

                $KUBECTL create namespace $APP_NS --dry-run=client -o yaml | $KUBECTL apply -f -

                $KUBECTL apply -n $APP_NS -f services/postgres/

                ./scripts/render-manifest.sh services/apiservice/k8s.yaml $IMAGE_TAG | $KUBECTL apply -n $APP_NS -f -
                ./scripts/render-manifest.sh services/authservice/k8s.yaml $IMAGE_TAG | $KUBECTL apply -n $APP_NS -f -
                ./scripts/render-manifest.sh services/userservice/k8s.yaml $IMAGE_TAG | $KUBECTL apply -n $APP_NS -f -
                ./scripts/render-manifest.sh services/frontend/k8s.yaml $IMAGE_TAG | $KUBECTL apply -n $APP_NS -f -
                '''
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
            echo "CI/CD SUCCESS – Application deployed with monitoring and alerts"
        }
        failure {
            echo "CI/CD FAILED – Check Jenkins logs"
        }
    }
}
