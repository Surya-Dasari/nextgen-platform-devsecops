pipeline {
agent any

```
environment {
    KUBECTL     = "/usr/local/bin/kubectl"
    DOCKER_REPO = "docker.io/suryadasari31"
    IMAGE_TAG   = "${BUILD_NUMBER}"
    K8S_CONTEXT = "kind-devops-lab"
    APP_NS      = "nextgen"
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
        echo "CI/CD SUCCESS – Application deployed"
    }
    failure {
        echo "CI/CD FAILED – Check Jenkins logs"
    }
}
```

}
