pipeline {
  agent any
  parameters {
    string(name: 'TARGET_HOST', defaultValue: '20.109.52.203', description: 'Target VM hostname or IP (e.g. 192.168.1.50)')
    string(name: 'TARGET_PORT', defaultValue: '22', description: 'SSH port for target VM (default 22)')
  }
  environment {
    JAVA_HOME      = "/usr/lib/jvm/java-21-openjdk-amd64" // adjust if different on your node
    PATH           = "${JAVA_HOME}/bin:${PATH}"
    BACKEND_IMAGE  = "yuridevpro/todo-app-java-backend"
    FRONTEND_IMAGE = "yuridevpro/todo-app-java-frontend"
    DOCKER_CREDS   = "dockerhub-creds"
    SSH_CREDS      = "target-vm-ssh"
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
            def app = docker.build("${BACKEND_IMAGE}:${TAG}", "-f backend/Dockerfile backend")
            app.push()
            app.push("latest")
          }
        }
      }
    }
    stage('Build & Push Frontend Image') {
      steps {
        script {
          docker.withRegistry('', DOCKER_CREDS) {
            def fe = docker.build("${FRONTEND_IMAGE}:${TAG}", "-f frontend/Dockerfile --build-arg VITE_API_URL=http://20.109.52.203:8080 frontend")
            fe.push()
            fe.push("latest")
          }
        }
      }
    }
    stage('Deploy to Target VM') {
      steps {
        script {
          if (!env.TARGET_HOST?.trim()) {
            error "TARGET_HOST is not set. Provide it as a build parameter."
          }
        }
        withCredentials([sshUserPrivateKey(credentialsId: SSH_CREDS, keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
          sh """
            scp -i ${SSH_KEY} -P ${params.TARGET_PORT} -o StrictHostKeyChecking=no docker-compose.deploy.yml ${SSH_USER}@${TARGET_HOST}:/tmp/todoapp-compose.deploy.yml
            ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no -p ${params.TARGET_PORT} ${SSH_USER}@${TARGET_HOST} '
              export IMAGE_TAG=${TAG}
              export ALLOWED_ORIGINS="http://${TARGET_HOST}:8081,http://${TARGET_HOST}"
              export VITE_API_URL="http://${TARGET_HOST}:8080"
              docker compose -f /tmp/todoapp-compose.deploy.yml down &&
              docker compose -f /tmp/todoapp-compose.deploy.yml pull &&
              docker compose -f /tmp/todoapp-compose.deploy.yml up -d
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
