pipeline {
  agent any
  environment {
    IMAGE          = "yuridevpro/todo-app-java-backend"
    DOCKER_CREDS   = "dockerhub-creds"
    SSH_CREDS      = "target-vm-ssh"
    TARGET_HOST    = "<TARGET_VM_IP>"
    TARGET_PORT    = "2222"
    CONTAINER_NAME = "todo-backend"
    TAG            = "build-${env.BUILD_NUMBER}"
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Unit Tests') {
      steps { dir('backend') { sh 'mvn -B test' } }
    }
    stage('Integration Tests') { // optional; requires docker compose
      steps { sh 'bash backend/scripts/run-integration-tests.sh' }
    }
    stage('Build & Push Image') {
      steps {
        script {
          docker.withRegistry('', DOCKER_CREDS) {
            def app = docker.build("${IMAGE}:${TAG}", "-f backend/Dockerfile backend")
            app.push()
            app.push("latest")
          }
        }
      }
    }
    stage('Deploy to Target VM') {
      steps {
        sshagent(credentials: [SSH_CREDS]) {
          sh """
            ssh -o StrictHostKeyChecking=no -p ${TARGET_PORT} deploy@${TARGET_HOST} '
              docker pull ${IMAGE}:${TAG} &&
              docker stop ${CONTAINER_NAME} || true &&
              docker rm ${CONTAINER_NAME} || true &&
              docker run -d --name ${CONTAINER_NAME} -p 80:8080 ${IMAGE}:${TAG}
            '
          """
        }
      }
    }
  }
  post {
    always { cleanWs() }
  }
}
