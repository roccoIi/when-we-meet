pipeline {
    agent any

    environment {
        IMAGE_TAG = "${BUILD_NUMBER}"
        BACKEND_IMAGE = "whenwemeet-backend"
        FRONTEND_IMAGE = "whenwemeet-frontend"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github_token',
                    url: 'https://github.com/roccoIi/when-we-meet.git'
            }
        }

        stage('Environment Setup') {
            steps {
                // 환경 변수 파일 복사
                withCredentials([file(credentialsId: 'env_secretfile_credential', variable: 'ENV_FILE')]) {
                    sh 'cp $ENV_FILE .env'
                    sh 'cp $ENV_FILE backend/.env'
                }
                
                withCredentials([file(credentialsId: 'frontend_env_secretfile', variable: 'FRONTEND_ENV')]) {
                    sh 'cp $FRONTEND_ENV frontend/.env'
                }
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('Build Backend') {
                    steps {
                        dir('backend') {
                            sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} ."
                            sh "docker tag ${BACKEND_IMAGE}:${IMAGE_TAG} ${BACKEND_IMAGE}:latest"
                        }
                    }
                }
                stage('Build Frontend') {
                    steps {
                        dir('frontend') {
                            sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} ."
                            sh "docker tag ${FRONTEND_IMAGE}:${IMAGE_TAG} ${FRONTEND_IMAGE}:latest"
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                // 기존 컨테이너 및 볼륨 정리
                sh 'docker-compose down || true'
                
                // 서비스 시작
                sh 'docker-compose up -d'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
