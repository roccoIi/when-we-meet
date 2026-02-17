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
                
                // 모니터링 설정 파일 검증
                sh '''
                    echo "🔍 모니터링 설정 파일 검증 중..."
                    
                    # 필수 파일 존재 확인
                    if [ ! -f "monitoring/prometheus/prometheus.yml" ]; then
                        echo "❌ monitoring/prometheus/prometheus.yml 파일이 없습니다!"
                        if [ -d "monitoring/prometheus/prometheus.yml" ]; then
                            echo "⚠️  디렉토리로 존재합니다. 삭제 후 재클론 필요!"
                            rm -rf monitoring/prometheus/prometheus.yml
                            git checkout -- monitoring/prometheus/prometheus.yml
                        fi
                    fi
                    
                    # 파일 타입 확인
                    file monitoring/prometheus/prometheus.yml || echo "파일 확인 실패"
                    
                    echo "✅ 설정 파일 검증 완료"
                '''
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
                sh 'docker-compose down -v || true'
                
                // Docker 시스템 정리 (캐시 제거)
                sh '''
                    echo "🧹 Docker 시스템 정리 중..."
                    # 사용하지 않는 컨테이너, 이미지, 볼륨 정리
                    docker system prune -f || true
                    # Prometheus 관련 볼륨 완전 삭제
                    docker volume ls -q | grep prometheus | xargs -r docker volume rm || true
                    # Grafana 관련 볼륨 완전 삭제  
                    docker volume ls -q | grep grafana | xargs -r docker volume rm || true
                '''
                
                // 잘못 생성된 디렉토리 정리 (파일이어야 하는데 디렉토리인 경우)
                sh '''
                    # Prometheus 설정 파일 체크
                    if [ -d "monitoring/prometheus/prometheus.yml" ]; then
                        echo "⚠️  prometheus.yml이 디렉토리로 존재합니다. 삭제합니다."
                        rm -rf monitoring/prometheus/prometheus.yml
                    fi
                    
                    # Grafana provisioning 파일들 체크
                    if [ -d "monitoring/grafana/provisioning/datasources/prometheus.yml" ]; then
                        echo "⚠️  datasources/prometheus.yml이 디렉토리로 존재합니다. 삭제합니다."
                        rm -rf monitoring/grafana/provisioning/datasources/prometheus.yml
                    fi
                    
                    if [ -d "monitoring/grafana/provisioning/dashboards/dashboard.yml" ]; then
                        echo "⚠️  dashboards/dashboard.yml이 디렉토리로 존재합니다. 삭제합니다."
                        rm -rf monitoring/grafana/provisioning/dashboards/dashboard.yml
                    fi
                    
                    # 파일이 제대로 존재하는지 확인
                    echo "📁 모니터링 설정 파일 확인:"
                    ls -lh monitoring/prometheus/prometheus.yml || echo "❌ prometheus.yml 없음"
                    ls -lh monitoring/grafana/provisioning/datasources/prometheus.yml || echo "❌ datasources/prometheus.yml 없음"
                    ls -lh monitoring/grafana/provisioning/dashboards/dashboard.yml || echo "❌ dashboard.yml 없음"
                    
                    # 절대 경로 확인
                    echo "📍 절대 경로:"
                    readlink -f monitoring/prometheus/prometheus.yml || echo "경로 확인 실패"
                '''
                
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
