# Vocabin Backend

영어 학습 웹앱 Vocabin의 백엔드 서버입니다.

## 기술 스택

| 항목 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA / Hibernate |
| Database | MariaDB |
| Build | Gradle |
| API 문서 | springdoc-openapi (Swagger UI) |

## 실행 방법

### Docker Compose (권장)

```bash
docker-compose up -d
```

### 로컬 직접 실행

MariaDB가 실행 중인 상태에서:

```bash
./gradlew bootRun
```

## API 문서

서버 실행 후 아래 URL에서 Swagger UI 확인:

```
http://localhost:8080/swagger-ui/index.html
```

## 패키지 구조

```
com.vocabin
├── domain/
│   ├── wordset/       # 주차별 단어 세트
│   ├── word/          # 단어/문장
│   ├── study/         # 학습 기록
│   └── schedule/      # 복습 일정 (SM-2)
├── application/       # UseCase, Service
├── infrastructure/    # JPA Repository, CSV 파서
└── api/               # Controller, DTO
```

## 주요 API

| Method | URL | 설명 |
|--------|-----|------|
| GET | /api/word-sets | 세트 목록 |
| POST | /api/word-sets | 세트 생성 |
| GET | /api/word-sets/{id}/words | 단어 목록 |
| POST | /api/word-sets/{id}/upload | CSV 업로드 |
| PUT | /api/words/{id} | 단어 수정 |
| DELETE | /api/words/{id} | 단어 삭제 |
| GET | /api/study/today | 오늘 복습할 단어 |
| GET | /api/study/weak | 취약 단어 목록 |
| POST | /api/study/record | 학습 결과 저장 |
| GET | /api/stats/summary | 전체 통계 요약 |
