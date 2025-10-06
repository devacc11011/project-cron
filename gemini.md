# Project Cron - Gemini AI 가이드

## 프로젝트 구조

이 프로젝트는 모노레포로 구성된 풀스택 크론잡 시스템입니다:

```
project-cron/
├── spring/   # Spring Boot 백엔드
└── next/     # Next.js 프론트엔드
```

---

## 백엔드 (spring)

### 기술 스택
- **프레임워크**: Spring Boot 3.5.6
- **언어**: Java 17
- **빌드 도구**: Gradle
- **주요 의존성**:
  - Spring Data JPA
  - Spring Security
  - MySQL Connector
  - H2 Database (개발용)
  - Lombok

### 코드 컨벤션

#### 패키지 구조
```
devacc11011.spring/
├── config/       # 설정 클래스
└── controller/   # REST 컨트롤러
```

#### Java 코딩 스타일
- **클래스명**: PascalCase (예: `Application`)
- **메서드/변수명**: camelCase
- **상수**: UPPER_SNAKE_CASE
- **들여쓰기**: 탭 사용
- **Lombok 사용**: `@Data`, `@Builder` 등 적극 활용

### 빌드 및 검증 명령어

```bash
cd spring

# 프로젝트 빌드 (테스트 포함)
./gradlew build

# 테스트만 실행
./gradlew test

# 빌드 클린
./gradlew clean

# 애플리케이션 실행
./gradlew bootRun
```

### 테스트 작성 규칙
⚠️ **모든 기능에 대해 유닛 테스트를 작성해야 함**
- Service 레이어의 모든 public 메서드는 테스트 필수
- Controller 레이어의 모든 엔드포인트는 테스트 필수
- Repository 레이어의 커스텀 쿼리는 테스트 필수
- 테스트 클래스명: `{클래스명}Test` (예: `ApplicationTest`)
- 테스트 메서드명: 한글 또는 영문으로 명확하게 작성
- Given-When-Then 패턴 권장

### 중요 규칙
⚠️ **모든 코드 변경 후 반드시 `./gradlew build` 성공 확인 필수**
- 빌드가 실패하면 커밋 불가
- 모든 테스트가 통과해야 함
- 새로운 기능 추가 시 반드시 유닛 테스트를 함께 작성

---

## 프론트엔드 (next)

### 기술 스택
- **프레임워크**: Next.js 15.5.4 (App Router)
- **언어**: TypeScript 5
- **런타임**: React 19.1.0
- **스타일링**: Tailwind CSS 4
- **린터**: ESLint 9 (Next.js config)

### 코드 컨벤션

#### 디렉토리 구조
```
next/
├── app/              # Next.js App Router
│   ├── layout.tsx
│   ├── page.tsx
│   └── globals.css
├── components/       # 재사용 가능한 컴포넌트
├── lib/              # 유틸리티 함수
├── public/           # 정적 파일
└── node_modules/
```

#### TypeScript/React 코딩 스타일
- **컴포넌트명**: PascalCase (예: `UserProfile.tsx`)
- **파일명**: kebab-case 또는 PascalCase
- **변수/함수**: camelCase
- **상수**: UPPER_SNAKE_CASE
- **들여쓰기**: 2칸 스페이스
- **타입 안정성**: `strict: true` 모드
- **경로 별칭**: `@/*`로 절대 경로 사용 가능
- **텍스트 언어**: 모든 UI 텍스트는 영어로 작성
- **Turbopack**: Next.js 빌드 및 개발 서버에서 Turbopack 사용

#### ESLint 설정
- **확장**: `next/core-web-vitals`, `next/typescript`
- **무시 경로**: `node_modules`, `.next`, `out`, `build`

### 빌드 및 검증 명령어

```bash
cd next

# 개발 서버 실행 (Turbopack 사용)
npm run dev

# 린트 검사
npm run lint

# 프로덕션 빌드 (Turbopack 사용)
npm run build

# 프로덕션 실행
npm run start
```

### 중요 규칙
⚠️ **모든 코드 변경 후 반드시 `npm run lint` 및 `npm run build` 성공 확인 필수**
- ESLint 에러가 있으면 커밋 불가
- 빌드가 실패하면 배포 불가
- 타입 체크도 빌드 시 자동 수행됨

---

## Gemini AI 작업 시 체크리스트

### 백엔드 작업 후
- [ ] 새로운 기능에 대한 유닛 테스트 작성
- [ ] `cd spring && ./gradlew test` 실행하여 테스트 통과 확인
- [ ] `cd spring && ./gradlew build` 실행
- [ ] 모든 테스트 통과 확인
- [ ] Java 컨벤션 준수 확인

### 프론트엔드 작업 후
- [ ] `cd next && npm run lint` 실행
- [ ] `cd next && npm run build` 실행
- [ ] TypeScript 에러 없는지 확인
- [ ] ESLint 에러 없는지 확인
- [ ] **개발 서버가 실행 중이라면 재시작 (Ctrl+C 후 `npm run dev`)**

### 전체 검증 스크립트
```bash
# 루트 디렉토리에서 실행
cd spring && ./gradlew build && cd ../next && npm run lint && npm run build
```

---

## 추가 정보

- **Git 브랜치**: `main`
- **백엔드 포트**: 8080 (기본값)
- **프론트엔드 포트**: 3000 (개발 모드)

### 도메인 정보
- **프론트엔드 URL**: https://acron.lisan-al-gaib.top/
- **백엔드 API URL**: https://acron-api.lisan-al-gaib.top/

---

## 문제 해결

### 백엔드 빌드 실패 시
1. `./gradlew clean` 실행
2. Java 버전 확인 (JDK 17)
3. 의존성 문제: `./gradlew --refresh-dependencies`

### 프론트엔드 빌드 실패 시
1. `rm -rf .next` 실행
2. `npm ci` (package-lock.json 기반 재설치)
3. Node.js 버전 확인 (20+)
