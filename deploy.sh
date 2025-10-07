#!/bin/bash

# 배포 스크립트
# 사용법: ./deploy.sh [브랜치명] [백엔드포트] [프론트엔드포트]
# 예시: ./deploy.sh main 8080 3000

set -e

# 기본 설정
GIT_URL="https://github.com/devacc11011/project-cron.git"
BRANCH=${1:-main}
BACKEND_PORT=${2:-8080}
FRONTEND_PORT=${3:-3000}
PROJECT_NAME="project-cron"
DEPLOY_DIR="/tmp/${PROJECT_NAME}-deploy"

echo "=========================================="
echo "Project Cron 배포 스크립트"
echo "=========================================="
echo "Git URL: ${GIT_URL}"
echo "브랜치: ${BRANCH}"
echo "백엔드 포트: ${BACKEND_PORT}"
echo "프론트엔드 포트: ${FRONTEND_PORT}"
echo "=========================================="

# 1. 기존 배포 디렉토리 정리
echo "[1/5] 배포 디렉토리 정리 중..."
if [ -d "$DEPLOY_DIR" ]; then
    rm -rf "$DEPLOY_DIR"
fi
mkdir -p "$DEPLOY_DIR"

# 2. Git 클론
echo "[2/5] Git 저장소에서 코드 가져오는 중..."
git clone -b "$BRANCH" "$GIT_URL" "$DEPLOY_DIR"
cd "$DEPLOY_DIR"

# 3. Docker 이미지 빌드
echo "[3/5] Docker 이미지 빌드 중..."
echo "  - 백엔드 이미지 빌드..."
docker build -f Dockerfile.backend -t "${PROJECT_NAME}-backend:${BRANCH}" .
echo "  - 프론트엔드 이미지 빌드..."
# Next.js 빌드를 위해 환경변수 파일 복사
if [ ! -f ~/.env.next ]; then
    echo "오류: ~/.env.next 파일을 찾을 수 없습니다."
    exit 1
fi
cp ~/.env.next "${DEPLOY_DIR}/next/.env"
docker build -f Dockerfile.frontend -t "${PROJECT_NAME}-frontend:${BRANCH}" .
rm -f "${DEPLOY_DIR}/next/.env"

# 4. 기존 컨테이너 중지 및 제거
echo "[4/5] 기존 컨테이너 중지 및 제거 중..."
if [ "$(docker ps -aq -f name=${PROJECT_NAME}-backend)" ]; then
    docker stop "${PROJECT_NAME}-backend" || true
    docker rm "${PROJECT_NAME}-backend" || true
fi
if [ "$(docker ps -aq -f name=${PROJECT_NAME}-frontend)" ]; then
    docker stop "${PROJECT_NAME}-frontend" || true
    docker rm "${PROJECT_NAME}-frontend" || true
fi

# 5. 새 컨테이너 실행
echo "[5/5] 컨테이너 실행 중..."

# 백엔드 컨테이너 실행
echo "  - 백엔드 컨테이너 실행..."
if [ ! -f ~/.env.spring ]; then
    echo "오류: ~/.env.spring 파일을 찾을 수 없습니다."
    exit 1
fi
docker run -d \
    --name "${PROJECT_NAME}-backend" \
    -p "${BACKEND_PORT}:8080" \
    --env-file ~/.env.spring \
    --restart unless-stopped \
    "${PROJECT_NAME}-backend:${BRANCH}"

# 프론트엔드 컨테이너 실행
echo "  - 프론트엔드 컨테이너 실행..."
if [ ! -f ~/.env.next ]; then
    echo "오류: ~/.env.next 파일을 찾을 수 없습니다."
    exit 1
fi
docker run -d \
    --name "${PROJECT_NAME}-frontend" \
    -p "${FRONTEND_PORT}:3000" \
    --env-file ~/.env.next \
    --restart unless-stopped \
    "${PROJECT_NAME}-frontend:${BRANCH}"

echo "=========================================="
echo "배포 완료!"
echo "=========================================="
echo "백엔드:"
echo "  - 컨테이너명: ${PROJECT_NAME}-backend"
echo "  - 포트: ${BACKEND_PORT}:8080"
echo "  - URL: http://localhost:${BACKEND_PORT}"
echo ""
echo "프론트엔드:"
echo "  - 컨테이너명: ${PROJECT_NAME}-frontend"
echo "  - 포트: ${FRONTEND_PORT}:3000"
echo "  - URL: http://localhost:${FRONTEND_PORT}"
echo "=========================================="
echo "로그 확인:"
echo "  - 백엔드: docker logs -f ${PROJECT_NAME}-backend"
echo "  - 프론트엔드: docker logs -f ${PROJECT_NAME}-frontend"
echo ""
echo "컨테이너 중지:"
echo "  - 백엔드: docker stop ${PROJECT_NAME}-backend"
echo "  - 프론트엔드: docker stop ${PROJECT_NAME}-frontend"
echo "  - 전체: docker stop ${PROJECT_NAME}-backend ${PROJECT_NAME}-frontend"
echo "=========================================="
