pipeline {
  agent any
  parameters {
    string(name: 'TARGET_HOST', defaultValue: '', description: 'Target VM hostname or IP (e.g. 192.168.1.50)')
    string(name: 'TARGET_PORT', defaultValue: '22', description: 'SSH port for target VM (default 22)')
  }
  environment {
    JAVA_HOME      = "/usr/lib/jvm/java-21-openjdk-amd64" // adjust if different on your node
    PATH           = "${JAVA_HOME}/bin:${PATH}"
    IMAGE          = "yuridevpro/todo-app-java-backend"
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
            def app = docker.build("${IMAGE}:${TAG}", "-f backend/Dockerfile backend")
            app.push()
            app.push("latest")
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
        sshagent(credentials: [SSH_CREDS]) {
          sh """
            ssh -o StrictHostKeyChecking=no -p ${params.TARGET_PORT} deploy@${TARGET_HOST} '
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
