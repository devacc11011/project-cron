# Project Cron - 배포 가이드

## 목차
1. [개요](#개요)
2. [초기 설정 (처음 시작하는 경우)](#초기-설정-처음-시작하는-경우)
3. [Jenkins 플러그인 설치](#jenkins-플러그인-설치)
4. [Jenkins 사용자 권한 설정](#jenkins-사용자-권한-설정)
5. [SSH 키 생성 및 배포](#ssh-키-생성-및-배포)
6. [원격 서버 설정](#원격-서버-설정)
7. [Jenkins Job 설정](#jenkins-job-설정)
8. [배포 실행](#배포-실행)
9. [로컬 테스트](#로컬-테스트)
10. [트러블슈팅](#트러블슈팅)

---

## 개요

이 프로젝트는 Jenkins를 통해 Docker 이미지를 빌드하고, SCP로 원격 서버에 전송하여 배포합니다.

**배포 흐름:**
```
Jenkins 서버
  ├─ 1. Git Checkout
  ├─ 2. Docker 이미지 빌드 (backend, frontend)
  ├─ 3. 이미지를 tar 파일로 저장
  └─ 4. SCP로 원격 서버에 전송
      └─ 원격 서버
          ├─ 5. tar 파일에서 이미지 로드
          ├─ 6. 기존 컨테이너 중지/삭제
          └─ 7. 새 컨테이너 실행
```

---

## 초기 설정 (처음 시작하는 경우)

### 1. Jenkins 초기 접속

Jenkins가 설치되었다면 기본적으로 `http://localhost:8080` 또는 `http://your-server-ip:8080`에서 접속 가능합니다.

**첫 접속 시:**

1. **Unlock Jenkins** 화면이 표시됩니다
2. 초기 관리자 비밀번호를 입력해야 합니다:

```bash
# Jenkins 서버에서 실행
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

3. 출력된 비밀번호를 복사하여 웹 페이지에 붙여넣기
4. **Continue** 클릭

### 2. Jenkins 초기 플러그인 설치

**Customize Jenkins** 화면에서:

1. **Install suggested plugins** 선택 (권장)
   - 기본 플러그인이 자동으로 설치됩니다
   - 설치 시간: 약 5-10분

2. 플러그인 설치 완료까지 대기

### 3. 관리자 계정 생성

**Create First Admin User** 화면에서:

1. 관리자 정보 입력:
   - Username: `admin` (또는 원하는 사용자명)
   - Password: 안전한 비밀번호
   - Confirm password: 비밀번호 재입력
   - Full name: 전체 이름
   - E-mail address: 이메일 주소

2. **Save and Continue** 클릭

### 4. Jenkins URL 설정

**Instance Configuration** 화면에서:

1. Jenkins URL 확인 (기본값 유지 또는 수정)
   - 예: `http://your-server-ip:8080/`

2. **Save and Finish** 클릭

3. **Start using Jenkins** 클릭

---

## Jenkins 플러그인 설치

초기 설치 후 배포에 필요한 추가 플러그인을 설치합니다.

### 1. 플러그인 관리 메뉴 접속

1. Jenkins 대시보드에서 **Jenkins 관리** (Manage Jenkins) 클릭
2. **Plugins** 클릭

### 2. 필수 플러그인 설치

**Available plugins** 탭에서 다음 플러그인을 검색하여 설치:

#### 2.1 Docker Pipeline 플러그인

1. 검색창에 `Docker Pipeline` 입력
2. **Docker Pipeline** 체크박스 선택
3. 페이지 하단 **Install** 클릭

#### 2.2 SSH Agent 플러그인

1. 검색창에 `SSH Agent` 입력
2. **SSH Agent Plugin** 체크박스 선택
3. 체크 유지

#### 2.3 Git 플러그인

1. 검색창에 `Git` 입력
2. **Git plugin** 체크박스 선택 (이미 설치되어 있을 수 있음)
3. 체크 유지

#### 2.4 Pipeline 플러그인

1. 검색창에 `Pipeline` 입력
2. **Pipeline** 체크박스 선택 (이미 설치되어 있을 수 있음)
3. 체크 유지

#### 2.5 설치 실행

1. 모든 플러그인 선택 후 페이지 하단으로 스크롤
2. **Install** 버튼 클릭
3. **Restart Jenkins when installation is complete and no jobs are running** 체크
4. 설치 및 재시작 대기 (약 2-5분)

### 3. 설치 확인

재시작 후 다시 로그인하여:

1. **Jenkins 관리** → **Plugins** → **Installed plugins**
2. 다음 플러그인이 설치되었는지 확인:
   - Docker Pipeline
   - SSH Agent Plugin
   - Git plugin
   - Pipeline

---

## Jenkins 사용자 권한 설정

Jenkins가 Docker 명령을 실행할 수 있도록 권한을 부여합니다.

### ⚠️ Jenkins 설치 방식 확인

먼저 Jenkins가 어떻게 설치되었는지 확인하세요:

**방법 1: Jenkins가 호스트에 직접 설치된 경우**
- systemd 서비스로 실행 중 (`systemctl status jenkins`)
- → **섹션 A** 참고

**방법 2: Jenkins가 Docker 컨테이너로 실행 중인 경우**
- `docker ps | grep jenkins` 로 확인
- → **섹션 B** 참고

---

### 섹션 A: Jenkins가 호스트에 직접 설치된 경우

#### 1. Jenkins 서버에 SSH 접속

```bash
# 로컬 머신에서 Jenkins 서버로 접속
ssh your-user@jenkins-server-ip
```

#### 2. Docker 그룹에 Jenkins 사용자 추가

```bash
# jenkins 사용자를 docker 그룹에 추가
sudo usermod -aG docker jenkins

# 변경사항 확인
groups jenkins
# 출력 예시: jenkins : jenkins docker
```

#### 3. Jenkins 재시작

```bash
sudo systemctl restart jenkins

# 재시작 확인
sudo systemctl status jenkins
```

#### 4. 권한 확인

```bash
# jenkins 사용자로 전환
sudo su - jenkins

# Docker 명령 테스트
docker ps
# 에러 없이 실행되면 성공

# 원래 사용자로 복귀
exit
```

---

### 섹션 B: Jenkins가 Docker 컨테이너로 실행 중인 경우 ✅

#### 1. 현재 Jenkins 컨테이너 확인

```bash
# Jenkins 서버 호스트에 SSH 접속
ssh your-user@jenkins-server-ip

# Jenkins 컨테이너 확인
docker ps | grep jenkins

# 컨테이너 이름 또는 ID 확인 (예: jenkins, jenkins-server 등)
```

#### 2. 방법 1: Docker Socket 마운트 방식 (권장)

Jenkins 컨테이너가 호스트의 Docker를 사용하도록 설정합니다.

**Step 1: 현재 Jenkins 컨테이너 중지 및 백업**

```bash
# 컨테이너 이름 확인 (예: jenkins)
JENKINS_CONTAINER_NAME="jenkins"

# Jenkins 데이터 위치 확인
docker inspect $JENKINS_CONTAINER_NAME | grep jenkins_home

# 컨테이너 중지
docker stop $JENKINS_CONTAINER_NAME

# 이미지 백업 (선택사항)
docker commit $JENKINS_CONTAINER_NAME jenkins-backup
```

**Step 2: Jenkins 컨테이너를 Docker Socket과 함께 재실행**

```bash
# 기존 컨테이너 삭제
docker rm $JENKINS_CONTAINER_NAME

# Docker socket을 마운트하여 Jenkins 재실행
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -u root \
  jenkins/jenkins:lts

# 또는 기존 jenkins_home을 사용하는 경우
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v /path/to/your/jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -u root \
  jenkins/jenkins:lts
```

**중요 옵션 설명:**
- `-v /var/run/docker.sock:/var/run/docker.sock`: 호스트의 Docker socket을 컨테이너에 마운트
- `-u root`: Jenkins를 root 사용자로 실행 (Docker 권한 필요)
- `-v jenkins_home:/var/jenkins_home`: Jenkins 데이터 영구 저장

**Step 3: Jenkins 컨테이너 내부에서 Docker CLI 설치**

```bash
# Jenkins 컨테이너 접속
docker exec -it jenkins bash

# Docker CLI 설치
apt-get update
apt-get install -y docker.io

# Docker 명령 테스트
docker ps
# 호스트의 Docker 컨테이너 목록이 표시되면 성공

# 컨테이너에서 나가기
exit
```

#### 3. 방법 2: Docker-in-Docker (DinD) 방식

별도의 Docker 데몬을 Jenkins 컨테이너 내부에서 실행하는 방식입니다.

**Step 1: Docker-in-Docker를 지원하는 Jenkins 이미지 사용**

```bash
# 기존 컨테이너 중지 및 삭제
docker stop jenkins
docker rm jenkins

# DinD 지원 Jenkins 실행
docker run -d \
  --name jenkins \
  --privileged \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts-jdk17
```

#### 4. 설정 확인

**Jenkins 웹 UI 접속 확인:**
```bash
# 브라우저에서 접속
http://jenkins-server-ip:8080

# 재시작 후 초기 비밀번호가 필요한 경우
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

**Docker 명령 테스트:**
```bash
# Jenkins 컨테이너 내부에서 테스트
docker exec -it jenkins docker ps

# 호스트의 컨테이너 목록이 표시되면 성공
```

#### 5. Docker Compose 사용 예시 (추천)

더 쉬운 관리를 위해 Docker Compose를 사용할 수 있습니다:

**jenkins-compose.yml 파일 생성:**

```yaml
version: '3.8'

services:
  jenkins:
    image: jenkins/jenkins:lts
    container_name: jenkins
    privileged: true
    user: root
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
    restart: unless-stopped

volumes:
  jenkins_home:
```

**실행:**

```bash
# Jenkins 시작
docker-compose -f jenkins-compose.yml up -d

# 로그 확인
docker-compose -f jenkins-compose.yml logs -f

# 중지
docker-compose -f jenkins-compose.yml down
```

#### 6. 트러블슈팅: Docker socket 권한 오류

만약 여전히 권한 오류가 발생하면:

```bash
# 호스트에서 Docker socket 권한 변경
sudo chmod 666 /var/run/docker.sock

# 또는 docker 그룹 GID 확인 후 컨테이너 재실행
getent group docker
# 출력 예: docker:x:999:

# Jenkins 컨테이너를 해당 GID로 실행
docker run -d \
  --name jenkins \
  --group-add 999 \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

---

## SSH 키 생성 및 배포

Jenkins에서 원격 배포 서버로 SSH 접속하기 위한 키를 생성합니다.

### ⚠️ Jenkins 설치 방식에 따라 다른 방법 사용

**Jenkins가 호스트에 직접 설치된 경우:** → **섹션 A** 참고
**Jenkins가 Docker 컨테이너인 경우:** → **섹션 B** 참고

---

### 섹션 A: Jenkins가 호스트에 직접 설치된 경우

#### 1. Jenkins 사용자로 SSH 키 생성

```bash
# Jenkins 서버에서 실행
sudo su - jenkins

# SSH 키 생성
ssh-keygen -t rsa -b 4096 -C "jenkins@project-cron"

# 프롬프트에서:
# Enter file in which to save the key: (엔터 - 기본 경로 사용)
# Enter passphrase: (엔터 - 비밀번호 없이 생성)
# Enter same passphrase again: (엔터)

# 키 생성 확인
ls -la ~/.ssh/
# id_rsa (개인키)
# id_rsa.pub (공개키)
```

#### 2. 개인키 내용 복사

```bash
# 개인키 내용 출력 (나중에 Jenkins에 등록할 때 사용)
cat ~/.ssh/id_rsa

# 전체 내용을 복사 (-----BEGIN ~ -----END 포함)
```

#### 3. 공개키를 원격 서버에 등록

```bash
# 공개키 확인
cat ~/.ssh/id_rsa.pub

# 원격 서버에 복사 (비밀번호 입력 필요)
ssh-copy-id your-user@remote-server-ip

# 또는 수동으로 복사
ssh your-user@remote-server-ip "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys" < ~/.ssh/id_rsa.pub

# SSH 연결 테스트
ssh your-user@remote-server-ip
# 비밀번호 없이 접속되면 성공
exit

# jenkins 사용자에서 나가기
exit
```

---

### 섹션 B: Jenkins가 Docker 컨테이너인 경우 ✅

#### 1. Jenkins 컨테이너 내부에서 SSH 키 생성

```bash
# Jenkins 컨테이너 접속
docker exec -it jenkins bash

# SSH 키 생성
ssh-keygen -t rsa -b 4096 -C "jenkins@project-cron"

# 프롬프트에서:
# Enter file in which to save the key: /var/jenkins_home/.ssh/id_rsa (경로 입력)
# Enter passphrase: (엔터 - 비밀번호 없이 생성)
# Enter same passphrase again: (엔터)

# .ssh 디렉토리 생성 (없는 경우)
mkdir -p /var/jenkins_home/.ssh
chmod 700 /var/jenkins_home/.ssh

# 키 생성 확인
ls -la /var/jenkins_home/.ssh/
```

#### 2. 개인키 내용 복사

```bash
# 컨테이너 내부에서 실행
cat /var/jenkins_home/.ssh/id_rsa

# 전체 내용을 복사 (-----BEGIN ~ -----END 포함)
# 메모장에 저장
```

출력 예시:
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAACFwAAAAdzc2gtcn
...
-----END OPENSSH PRIVATE KEY-----
```

**중요: 이 내용을 메모장에 복사해두세요. 나중에 Jenkins Credentials에 등록합니다.**

#### 3. 공개키 내용 복사

```bash
# 컨테이너 내부에서 실행
cat /var/jenkins_home/.ssh/id_rsa.pub
```

출력 예시:
```
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC... jenkins@project-cron
```

**중요: 이 내용도 메모장에 복사해두세요.**

```bash
# 컨테이너에서 나가기
exit
```

#### 4. 원격 서버에 공개키 등록

**방법 1: 수동 등록**

```bash
# 원격 배포 서버에 SSH 접속
ssh your-user@remote-server-ip

# .ssh 디렉토리 생성
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# authorized_keys 파일 편집
nano ~/.ssh/authorized_keys

# 위에서 복사한 공개키(id_rsa.pub 내용)를 새 줄에 붙여넣기
# Ctrl + O (저장), Enter, Ctrl + X (나가기)

# 파일 권한 설정
chmod 600 ~/.ssh/authorized_keys

# 로그아웃
exit
```

**방법 2: echo 명령으로 추가**

```bash
# 로컬 또는 Jenkins 호스트에서 실행
# 공개키를 변수에 저장 (위에서 복사한 내용)
PUBLIC_KEY="ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC... jenkins@project-cron"

# 원격 서버에 추가
ssh your-user@remote-server-ip "mkdir -p ~/.ssh && echo '$PUBLIC_KEY' >> ~/.ssh/authorized_keys && chmod 700 ~/.ssh && chmod 600 ~/.ssh/authorized_keys"
```

#### 5. SSH 연결 테스트

**Jenkins 컨테이너에서 테스트:**

```bash
# Jenkins 컨테이너 접속
docker exec -it jenkins bash

# SSH 클라이언트 설치 (없는 경우)
apt-get update && apt-get install -y openssh-client

# 원격 서버에 SSH 접속 시도
ssh -i /var/jenkins_home/.ssh/id_rsa your-user@remote-server-ip

# 첫 접속 시 fingerprint 확인 메시지:
# Are you sure you want to continue connecting (yes/no)?
# → yes 입력

# 비밀번호 없이 접속되면 성공!
# 호스트명 확인
hostname

# 접속 종료
exit

# 컨테이너에서 나가기
exit
```

**연결 실패 시:**

```bash
# Jenkins 컨테이너에서 디버깅
docker exec -it jenkins bash

# SSH 상세 로그로 연결 시도
ssh -vvv -i /var/jenkins_home/.ssh/id_rsa your-user@remote-server-ip

# 권한 확인
ls -la /var/jenkins_home/.ssh/
# id_rsa 파일 권한이 600이어야 함

# 권한 수정
chmod 600 /var/jenkins_home/.ssh/id_rsa
chmod 644 /var/jenkins_home/.ssh/id_rsa.pub

exit
```

#### 6. known_hosts 파일 사전 추가 (선택사항)

첫 SSH 접속 시 "yes" 입력을 자동화하려면:

```bash
# Jenkins 컨테이너 접속
docker exec -it jenkins bash

# known_hosts에 원격 서버 추가
ssh-keyscan -H remote-server-ip >> /var/jenkins_home/.ssh/known_hosts

# 확인
cat /var/jenkins_home/.ssh/known_hosts

exit
```

---

## 원격 서버 설정

원격 배포 서버에서 Docker 설치 및 환경을 설정합니다.

### 1. Docker가 이미 설치되어 있는지 확인

```bash
# 원격 서버에 SSH 접속
ssh your-user@remote-server-ip

# Docker 버전 확인
docker --version

# Docker 실행 확인
docker ps
```

**Docker가 이미 설치되어 있고 정상 작동하면 다음 섹션으로 이동하세요.**

### 2. Docker 권한 확인 및 설정

```bash
# 현재 사용자를 docker 그룹에 추가 (권한 에러가 발생하는 경우)
sudo usermod -aG docker $USER

# 변경사항 적용 (재로그인 없이)
newgrp docker

# 권한 확인
docker ps
# 에러 없이 실행되면 성공
```

### 3. 배포용 디렉토리 생성

```bash
# 배포용 디렉토리 생성
mkdir -p ~/project-cron

# 디렉토리 확인
ls -la ~/project-cron
```

### 4. Docker 네트워크 생성

```bash
# 네트워크 생성 (Jenkins가 자동으로 생성하지만 사전 생성 권장)
docker network create project-cron-network

# 네트워크 확인
docker network ls | grep project-cron
```

### 5. 방화벽 설정 (필요한 경우)

**Ubuntu (ufw):**
```bash
sudo ufw allow 8080/tcp
sudo ufw allow 3000/tcp
sudo ufw reload
```

**Amazon Linux (firewalld):**
```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --reload
```

**AWS Security Group:**
- 인바운드 규칙에 8080, 3000 포트 추가

### 6. 원격 서버 설정 완료 확인

```bash
# 모든 설정 확인
echo "=== Docker 버전 ==="
docker --version

echo "=== Docker 실행 상태 ==="
sudo systemctl status docker | grep Active

echo "=== Docker 네트워크 ==="
docker network ls | grep project-cron

echo "=== 배포 디렉토리 ==="
ls -la ~/project-cron

echo "=== SSH authorized_keys ==="
ls -la ~/.ssh/authorized_keys
```

---

## Jenkins Job 설정

이제 Jenkins에서 배포 Job을 생성합니다.

### 1. Jenkins Credentials 등록

**Step 1: Credentials 관리 페이지 접속**

1. Jenkins 대시보드에서 **Jenkins 관리** (Manage Jenkins) 클릭
2. **Credentials** 클릭
3. **System** 클릭
4. **Global credentials (unrestricted)** 클릭
5. 왼쪽 메뉴에서 **Add Credentials** 클릭

**Step 2: SSH Credentials 등록**

1. **Kind** 드롭다운에서 **SSH Username with private key** 선택

2. 정보 입력:
   - **ID**: `ssh-credentials`
     - (중요: Jenkinsfile에서 이 ID를 사용합니다)

   - **Description**: `SSH key for remote deployment server`
     - (설명, 선택사항)

   - **Username**: 원격 서버 사용자명 입력
     - 예: `ubuntu`, `ec2-user`, `root` 등

   - **Private Key**: **Enter directly** 선택
     - **Add** 버튼 클릭
     - 앞에서 복사해둔 개인키 전체를 붙여넣기
     - `-----BEGIN OPENSSH PRIVATE KEY-----` 부터
     - `-----END OPENSSH PRIVATE KEY-----` 까지 전부 포함

3. **Create** 버튼 클릭

**Step 3: 등록 확인**

- Global credentials 목록에 `ssh-credentials`가 표시되는지 확인
- Username과 Kind가 올바른지 확인

### 2. Git Repository 설정 (GitHub/GitLab 사용 시)

**프로젝트를 Git Repository에 푸시해야 합니다.**

```bash
# 로컬 프로젝트 디렉토리에서
cd /Users/seongwoncha/Desktop/workspace/project-cron

# Git 저장소 초기화 (아직 안했다면)
git init

# 원격 저장소 추가 (본인의 Git URL로 변경)
git remote add origin https://github.com/your-username/project-cron.git

# 모든 파일 추가
git add .

# 커밋
git commit -m "Initial commit with deployment configuration"

# 푸시
git push -u origin main
```

**Git Credentials 등록 (Private Repository인 경우)**

1. **Jenkins 관리** → **Credentials** → **Global credentials**
2. **Add Credentials** 클릭
3. **Kind**: Username with password 선택
4. 정보 입력:
   - **Username**: GitHub/GitLab 사용자명
   - **Password**: Personal Access Token (GitHub) 또는 비밀번호
   - **ID**: `git-credentials`
5. **Create** 클릭

### 3. Pipeline Job 생성

**Step 1: 새 Job 생성**

1. Jenkins 대시보드에서 **New Item** 클릭
2. **Enter an item name**: `project-cron-deploy` 입력
3. **Pipeline** 선택
4. **OK** 클릭

**Step 2: General 설정**

1. **Description**: `Project Cron 배포 파이프라인` 입력 (선택사항)
2. **GitHub project** (선택사항): Git 저장소 URL 입력

**Step 3: Build Triggers 설정 (선택사항)**

자동 빌드를 원하는 경우:

- **GitHub hook trigger for GITScm polling** 체크
  - Git Push 시 자동 빌드
- 또는 **Poll SCM** 체크하고 스케줄 입력
  - 예: `H/5 * * * *` (5분마다 체크)

**Step 4: Pipeline 설정**

1. **Definition**: **Pipeline script from SCM** 선택

2. **SCM**: **Git** 선택

3. **Repositories** 섹션:
   - **Repository URL**: Git 저장소 URL 입력
     - 예: `https://github.com/your-username/project-cron.git`

   - **Credentials**:
     - Public Repository: `-none-` 선택
     - Private Repository: `git-credentials` 선택

4. **Branches to build**:
   - **Branch Specifier**: `*/main` 입력

5. **Script Path**: `Jenkinsfile` 입력

6. **Save** 클릭

### 4. Jenkinsfile 환경변수 수정

배포 전에 `Jenkinsfile`의 환경변수를 실제 환경에 맞게 수정해야 합니다.

**Step 1: Git 저장소에서 Jenkinsfile 편집**

로컬에서 `Jenkinsfile`을 열고 수정:

```groovy
environment {
    REMOTE_HOST = '123.123.123.123'  // 실제 원격 서버 IP로 변경
    REMOTE_USER = 'ubuntu'           // 실제 원격 서버 사용자명으로 변경
    SSH_CREDENTIALS_ID = 'ssh-credentials'  // Jenkins에 등록한 Credentials ID (변경 불필요)

    BACKEND_IMAGE = "project-cron-backend"
    FRONTEND_IMAGE = "project-cron-frontend"
}
```

**Step 2: 변경사항 커밋 및 푸시**

```bash
git add Jenkinsfile
git commit -m "Jenkins 환경변수 설정 업데이트"
git push origin main
```

### 5. 첫 빌드 테스트

**Step 1: 빌드 실행**

1. Jenkins 대시보드에서 `project-cron-deploy` Job 선택
2. 왼쪽 메뉴에서 **Build Now** 클릭

**Step 2: 빌드 진행 상황 확인**

1. **Build History**에서 `#1` (빌드 번호) 클릭
2. **Console Output** 클릭하여 실시간 로그 확인

**Step 3: 각 Stage 확인**

Console Output에서 다음 Stage들이 순서대로 실행됨:

1. ✓ **Checkout** - Git 코드 가져오기
2. ✓ **Build Docker Images** - Backend, Frontend 이미지 빌드
3. ✓ **Save Docker Images** - 이미지를 tar 파일로 저장
4. ✓ **Transfer Images to Remote Server** - SCP로 원격 전송
5. ✓ **Deploy on Remote Server** - 원격 서버에서 컨테이너 실행

**Step 4: 빌드 성공 확인**

- 모든 Stage에 녹색 체크마크
- Console Output 마지막에 `Finished: SUCCESS` 표시

---

## 배포 실행

### 1. 일반적인 배포 프로세스

코드를 수정한 후 배포하는 일반적인 과정:

**Step 1: 코드 수정 후 Git에 푸시**

```bash
# 로컬에서 코드 수정 후
cd /Users/seongwoncha/Desktop/workspace/project-cron

# 변경사항 확인
git status

# 변경 파일 추가
git add .

# 커밋
git commit -m "기능 추가 또는 버그 수정"

# 푸시
git push origin main
```

**Step 2: Jenkins에서 빌드 실행**

1. 브라우저에서 Jenkins 접속
2. `project-cron-deploy` Job 클릭
3. 왼쪽 메뉴에서 **Build Now** 클릭
4. Build History에서 새 빌드 번호 클릭 (예: #2, #3...)
5. **Console Output** 클릭하여 실시간 로그 확인
6. 모든 Stage가 완료될 때까지 대기 (약 5-10분)

**Step 3: 배포 완료 확인**

- Console Output에 `Finished: SUCCESS` 표시
- 또는 Job 페이지에 녹색 체크마크 표시

### 2. 배포 결과 확인

원격 서버에 SSH 접속하여 확인:

**Step 1: 원격 서버 접속**

```bash
# 로컬에서 원격 서버로 SSH 접속
ssh your-user@remote-server-ip
```

**Step 2: 컨테이너 실행 상태 확인**

```bash
# 실행 중인 컨테이너 확인
docker ps

# 예상 출력:
# CONTAINER ID   IMAGE                          STATUS         PORTS                    NAMES
# abc123         project-cron-backend:latest    Up 2 minutes   0.0.0.0:8080->8080/tcp   project-cron-backend
# def456         project-cron-frontend:latest   Up 2 minutes   0.0.0.0:3000->3000/tcp   project-cron-frontend
```

**정상 작동 확인:**
- STATUS가 "Up"으로 표시됨
- 2개의 컨테이너(backend, frontend)가 실행 중

**Step 3: 로그 확인**

```bash
# 백엔드 로그 확인 (최근 50줄)
docker logs --tail 50 project-cron-backend

# 프론트엔드 로그 확인 (최근 50줄)
docker logs --tail 50 project-cron-frontend

# 실시간 로그 확인 (Ctrl+C로 종료)
docker logs -f project-cron-backend
```

**Step 4: 애플리케이션 접속 테스트**

```bash
# 백엔드 헬스체크
curl http://localhost:8080/health
# 예상 출력: {"status":"UP"} 또는 OK

# 프론트엔드 접속 (HTML이 출력되면 정상)
curl http://localhost:3000
```

**Step 5: 브라우저에서 접속**

로컬 브라우저에서 다음 URL 접속:

- 백엔드: `http://remote-server-ip:8080`
- 프론트엔드: `http://remote-server-ip:3000`

### 3. 배포 실패 시 대처

**빌드가 실패한 경우:**

1. Jenkins Console Output에서 에러 메시지 확인
2. 아래 트러블슈팅 섹션 참고
3. 문제 해결 후 다시 **Build Now** 클릭

---

## 로컬 테스트

배포 전 로컬에서 Docker 이미지를 테스트할 수 있습니다.

### 1. Docker Compose 사용

```bash
# 프로젝트 루트에서 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down
```

### 2. 개별 이미지 빌드 및 실행

**백엔드:**
```bash
cd spring
docker build -t project-cron-backend:latest .
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name project-cron-backend \
  project-cron-backend:latest
```

**프론트엔드:**
```bash
cd next
docker build -t project-cron-frontend:latest .
docker run -d -p 3000:3000 \
  -e NODE_ENV=production \
  --name project-cron-frontend \
  project-cron-frontend:latest
```

---

## 트러블슈팅

### 1. Jenkins 빌드 실패

#### 문제 1: Docker 권한 오류

**에러 메시지:**
```
ERROR: Got permission denied while trying to connect to the Docker daemon socket
```

**원인:** Jenkins 사용자가 Docker 실행 권한이 없음

**해결 방법:**

```bash
# Jenkins 서버에서 실행
sudo usermod -aG docker jenkins

# Jenkins 재시작
sudo systemctl restart jenkins

# 또는 웹 UI에서
# http://your-jenkins-url/safeRestart

# 권한 확인
sudo su - jenkins
docker ps
exit
```

#### 문제 2: SSH 연결 실패

**에러 메시지:**
```
ERROR: Failed to connect to remote host
Permission denied (publickey)
```

**원인:** SSH 키 설정 또는 Credentials 문제

**해결 방법:**

**Step 1: SSH 연결 수동 테스트**
```bash
# Jenkins 서버에서
sudo su - jenkins
ssh your-user@remote-server-ip
# 연결되지 않으면 SSH 키 문제
exit
```

**Step 2: SSH 키 재설정**
```bash
# Jenkins 사용자로 전환
sudo su - jenkins

# 공개키 확인
cat ~/.ssh/id_rsa.pub

# 원격 서버에 공개키가 등록되어 있는지 확인
ssh your-user@remote-server-ip "cat ~/.ssh/authorized_keys"
# 위 공개키가 목록에 있어야 함
```

**Step 3: Jenkins Credentials 재확인**
1. Jenkins 관리 → Credentials
2. ssh-credentials 클릭
3. Username이 원격 서버 사용자명과 일치하는지 확인
4. Private Key가 올바른지 확인

#### 문제 3: Git Repository 접근 실패

**에러 메시지:**
```
ERROR: Couldn't find any revision to build
fatal: could not read Username
```

**원인:** Git Repository 접근 권한 문제

**해결 방법:**

**Private Repository인 경우:**
1. Jenkins 관리 → Credentials → Global credentials → Add
2. Kind: Username with password
3. Username: GitHub/GitLab 사용자명
4. Password: Personal Access Token
5. ID: git-credentials
6. Job 설정에서 Credentials 선택

**Public Repository인 경우:**
- Repository URL이 정확한지 확인
- HTTPS URL 사용 (SSH 말고)

#### 문제 4: Docker 이미지 빌드 실패

**에러 메시지:**
```
ERROR: failed to solve: failed to fetch ...
```

**원인:** Dockerfile 오류 또는 네트워크 문제

**해결 방법:**

```bash
# Jenkins 서버에서 수동으로 빌드 테스트
cd /path/to/workspace/project-cron/spring
docker build -t test-backend .

# 에러 메시지 확인 후 Dockerfile 수정
```

### 2. 원격 서버 배포 실패

#### 문제 1: SCP 전송 실패

**에러 메시지:**
```
scp: Connection refused
lost connection
```

**해결 방법:**

```bash
# 원격 서버에서 SSH 서비스 확인
sudo systemctl status sshd
# 또는
sudo systemctl status ssh

# 실행되지 않으면 시작
sudo systemctl start sshd
sudo systemctl enable sshd
```

#### 문제 2: Docker 이미지 로드 실패

**에러 메시지:**
```
Error processing tar file
```

**해결 방법:**

```bash
# 원격 서버에서 tar 파일 확인
ls -lh ~/project-cron/*.tar

# tar 파일 크기가 0이거나 너무 작으면 전송 실패
# Jenkins에서 다시 빌드

# 수동으로 이미지 로드 시도
docker load -i ~/project-cron/backend.tar
# 에러 메시지 확인
```

#### 문제 3: 컨테이너 실행 실패

**에러 메시지:**
```
Error starting userland proxy: listen tcp 0.0.0.0:8080: bind: address already in use
```

**원인:** 포트가 이미 사용 중

**해결 방법:**

```bash
# 원격 서버에서 실행
# 포트 사용 중인 프로세스 확인
sudo lsof -i :8080
sudo lsof -i :3000

# 기존 컨테이너 확인
docker ps -a | grep project-cron

# 기존 컨테이너 중지 및 삭제
docker stop project-cron-backend project-cron-frontend
docker rm project-cron-backend project-cron-frontend

# 다시 Jenkins에서 빌드
```

#### 문제 4: 컨테이너가 바로 종료됨

**증상:**
```bash
docker ps  # 아무것도 표시 안됨
docker ps -a  # Exited (1) 상태로 표시
```

**해결 방법:**

```bash
# 로그 확인
docker logs project-cron-backend
docker logs project-cron-frontend

# 일반적인 원인:
# 1. 환경변수 문제
# 2. 애플리케이션 시작 오류
# 3. 포트 충돌

# 로그에서 에러 확인 후 해결
```

### 3. 네트워크 문제

#### 문제 1: 백엔드-프론트엔드 통신 실패

**증상:** 프론트엔드에서 백엔드 API 호출 실패

**해결 방법:**

```bash
# 원격 서버에서 실행
# 네트워크 확인
docker network ls | grep project-cron

# 컨테이너가 같은 네트워크에 있는지 확인
docker network inspect project-cron-network

# 네트워크 재생성
docker stop project-cron-backend project-cron-frontend
docker rm project-cron-backend project-cron-frontend
docker network rm project-cron-network
docker network create project-cron-network

# Jenkins에서 다시 배포
```

#### 문제 2: 외부에서 접속 불가

**증상:** 브라우저에서 `http://remote-server-ip:8080` 접속 안됨

**해결 방법:**

**Step 1: 컨테이너 실행 확인**
```bash
docker ps
# PORTS 컬럼에 0.0.0.0:8080->8080/tcp 표시되어야 함
```

**Step 2: 방화벽 확인**
```bash
# Ubuntu (ufw)
sudo ufw status
sudo ufw allow 8080/tcp
sudo ufw allow 3000/tcp

# CentOS (firewalld)
sudo firewall-cmd --list-all
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --reload
```

**Step 3: 클라우드 Security Group 확인 (AWS/GCP/Azure)**
- 인바운드 규칙에 8080, 3000 포트 추가
- Source: 0.0.0.0/0 (모든 IP 허용)

### 4. 디스크 공간 부족

**에러 메시지:**
```
no space left on device
```

**해결 방법:**

```bash
# 디스크 사용량 확인
df -h

# Docker 디스크 사용량 확인
docker system df

# 사용하지 않는 이미지/컨테이너 정리
docker system prune -a -f

# 볼륨 정리
docker volume prune -f

# 빌드 캐시 정리
docker builder prune -a -f

# 재확인
docker system df
```

### 5. Jenkins 플러그인 문제

**증상:** Pipeline 실행 중 알 수 없는 에러

**해결 방법:**

```bash
# 1. Jenkins 관리 → Plugins → Updates
# 2. 모든 플러그인 업데이트
# 3. Jenkins 재시작
```

### 6. 메모리 부족

**에러 메시지:**
```
Cannot allocate memory
OutOfMemoryError
```

**해결 방법:**

```bash
# 실행 중인 프로세스 확인
free -h
top

# 불필요한 컨테이너 중지
docker stop $(docker ps -q)

# 시스템 리소스 확인
htop
```

### 7. 일반적인 디버깅 절차

**빌드 실패 시:**

1. **Jenkins Console Output 전체 읽기**
   - 어느 Stage에서 실패했는지 확인
   - 에러 메시지 복사

2. **수동으로 해당 단계 재현**
   ```bash
   # Jenkins 서버에서
   sudo su - jenkins
   cd /var/lib/jenkins/workspace/project-cron-deploy
   # 실패한 명령어 직접 실행
   ```

3. **로그 확인**
   ```bash
   # Jenkins 로그
   sudo tail -f /var/log/jenkins/jenkins.log

   # Docker 로그
   docker logs project-cron-backend
   ```

4. **권한 확인**
   ```bash
   # 파일 권한
   ls -la

   # 사용자 권한
   groups jenkins
   groups $USER
   ```

5. **재시도**
   - 문제 해결 후 Jenkins에서 **Build Now** 다시 클릭

---

## 유용한 명령어

### Docker 관리

```bash
# 모든 컨테이너 중지
docker stop $(docker ps -aq)

# 모든 컨테이너 삭제
docker rm $(docker ps -aq)

# 모든 이미지 삭제
docker rmi $(docker images -q)

# 로그 확인 (최근 100줄)
docker logs --tail 100 project-cron-backend

# 컨테이너 내부 접속
docker exec -it project-cron-backend sh
```

### 모니터링

```bash
# 리소스 사용량 확인
docker stats

# 특정 컨테이너 리소스 사용량
docker stats project-cron-backend project-cron-frontend
```

### 수동 배포

Jenkins 없이 수동으로 배포하려면:

```bash
# 1. 로컬에서 이미지 빌드
docker build -t project-cron-backend:latest ./spring
docker build -t project-cron-frontend:latest ./next

# 2. 이미지 저장
docker save -o backend.tar project-cron-backend:latest
docker save -o frontend.tar project-cron-frontend:latest

# 3. 원격 서버로 전송
scp backend.tar frontend.tar user@remote-host:~/project-cron/

# 4. 원격 서버에서 배포
ssh user@remote-host
cd ~/project-cron
docker load -i backend.tar
docker load -i frontend.tar
docker network create project-cron-network || true
docker stop project-cron-backend project-cron-frontend || true
docker rm project-cron-backend project-cron-frontend || true
docker run -d --name project-cron-backend --network project-cron-network -p 8080:8080 project-cron-backend:latest
docker run -d --name project-cron-frontend --network project-cron-network -p 3000:3000 project-cron-frontend:latest
```

---

## 추가 고려사항

### 1. 환경변수 관리

프로덕션 환경에서는 민감한 정보를 환경변수 파일로 관리:

```bash
# .env 파일 생성 (원격 서버)
cat > ~/project-cron/.env <<EOF
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:mysql://localhost:3306/db
DATABASE_USER=user
DATABASE_PASSWORD=password
NODE_ENV=production
NEXT_PUBLIC_API_URL=http://backend:8080
EOF

# Docker 실행 시 env 파일 사용
docker run -d --name project-cron-backend \
  --env-file ~/project-cron/.env \
  --network project-cron-network \
  -p 8080:8080 \
  project-cron-backend:latest
```

### 2. 로그 관리

```bash
# Docker 로그 로테이션 설정
docker run -d --name project-cron-backend \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  -p 8080:8080 \
  project-cron-backend:latest
```

### 3. 헬스체크

Docker Compose에 헬스체크 추가:

```yaml
services:
  backend:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

### 4. 무중단 배포

Blue-Green 배포를 위한 스크립트:

```bash
# 새 컨테이너 실행 (다른 포트)
docker run -d --name project-cron-backend-new -p 8081:8080 project-cron-backend:latest

# 헬스체크 확인
curl http://localhost:8081/health

# 성공하면 포트 전환 (nginx/load balancer 설정 변경)
# 기존 컨테이너 중지
docker stop project-cron-backend
docker rm project-cron-backend
docker rename project-cron-backend-new project-cron-backend
```

---

## 참고 문서

- [Docker 공식 문서](https://docs.docker.com/)
- [Jenkins 공식 문서](https://www.jenkins.io/doc/)
- [Spring Boot Docker 가이드](https://spring.io/guides/gs/spring-boot-docker/)
- [Next.js Docker 가이드](https://nextjs.org/docs/deployment#docker-image)
