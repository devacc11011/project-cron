pipeline {
    agent any

    environment {
        REMOTE_HOST = 'your-remote-host'  // 원격 머신 IP 또는 도메인
        REMOTE_USER = 'your-remote-user'  // 원격 머신 사용자
        SSH_CREDENTIALS_ID = 'ssh-credentials'  // Jenkins에 등록된 SSH credentials ID

        BACKEND_IMAGE = "project-cron-backend"
        FRONTEND_IMAGE = "project-cron-frontend"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    // Build backend image
                    sh "docker build -t ${BACKEND_IMAGE}:latest ./spring"

                    // Build frontend image
                    sh "docker build -t ${FRONTEND_IMAGE}:latest ./next"
                }
            }
        }

        stage('Save Docker Images') {
            steps {
                script {
                    // Save images as tar files
                    sh "docker save -o backend.tar ${BACKEND_IMAGE}:latest"
                    sh "docker save -o frontend.tar ${FRONTEND_IMAGE}:latest"
                }
            }
        }

        stage('Transfer Images to Remote Server') {
            steps {
                sshagent(credentials: ["${SSH_CREDENTIALS_ID}"]) {
                    sh """
                        # Create remote directory
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} 'mkdir -p ~/project-cron'

                        # Transfer image tar files
                        scp backend.tar ${REMOTE_USER}@${REMOTE_HOST}:~/project-cron/
                        scp frontend.tar ${REMOTE_USER}@${REMOTE_HOST}:~/project-cron/
                    """
                }
            }
        }

        stage('Deploy on Remote Server') {
            steps {
                sshagent(credentials: ["${SSH_CREDENTIALS_ID}"]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} '
                            cd ~/project-cron

                            # Load Docker images
                            docker load -i backend.tar
                            docker load -i frontend.tar

                            # Create network if not exists
                            docker network create project-cron-network || true

                            # Stop and remove existing containers
                            docker stop project-cron-backend project-cron-frontend || true
                            docker rm project-cron-backend project-cron-frontend || true

                            # Run backend container
                            docker run -d \
                                --name project-cron-backend \
                                --network project-cron-network \
                                -p 8080:8080 \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                --restart unless-stopped \
                                ${BACKEND_IMAGE}:latest

                            # Run frontend container
                            docker run -d \
                                --name project-cron-frontend \
                                --network project-cron-network \
                                -p 3000:3000 \
                                -e NODE_ENV=production \
                                -e NEXT_PUBLIC_API_URL=http://project-cron-backend:8080 \
                                --restart unless-stopped \
                                ${FRONTEND_IMAGE}:latest

                            # Clean up tar files and old images
                            rm -f backend.tar frontend.tar
                            docker image prune -f
                        '
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment completed successfully!'
        }
        failure {
            echo 'Deployment failed!'
        }
        always {
            // Clean up local tar files
            sh """
                rm -f backend.tar frontend.tar
            """
        }
    }
}
