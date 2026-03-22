# Vocabin 프로젝트 전용 지침

퀴즐렛 단어를 주차별로 관리하고 플래시카드 / 스피드 런 / 취약 단어 모드로 복습하는 개인용 영어 학습 웹앱.

---

## 프로젝트 구조

```
vocabin/
├── backend/    # Spring Boot (Java)
├── frontend/   # Next.js + Tailwind CSS
└── docker-compose.yml
```

---

## 백엔드 (Spring Boot)

### 패키지 구조

```
com.vocabin
├── domain/
│   ├── wordset/       # 주차별 단어 세트
│   ├── word/          # 단어/문장
│   ├── study/         # 학습 기록
│   └── schedule/      # 복습 일정
├── application/       # UseCase, Service
├── infrastructure/    # JPA Repository, CSV 파서
└── api/               # Controller, DTO
```

### 기술 스택

| 항목 | 버전/기술 |
|------|-----------|
| Java | 21 |
| Spring Boot | 3.x |
| Spring Data JPA | Hibernate |
| DB | MariaDB |
| API 문서 | springdoc-openapi (Swagger UI) |
| 빌드 | Gradle |

### 도메인 규칙

- `WordSet` : 주차별 단어 세트 (예: "1주차")
- `Word` : 영어/한국어 쌍, `WordSet`에 속함
- `StudyRecord` : 학습 결과 기록 (mode: flashcard / speedrun / weak)
- `ReviewSchedule` : SM-2 기반 복습 일정 (interval, ease_factor)

### SM-2 알고리즘 규칙

```
초기값: interval=1일, ease_factor=2.5
정답: interval = interval × ease_factor (올림), ease_factor 유지 또는 소폭 증가
오답: interval = 1, ease_factor 감소 (최소 1.3)
```

- `ReviewSchedule`은 도메인 메서드로 갱신 — 서비스에서 직접 필드 조작 금지
- ease_factor 하한선 1.3 반드시 준수

### API 규칙

- Swagger 필수: 모든 Controller에 `@Tag`, `@Operation` 적용
- 모든 요청/응답 DTO에 `@Schema` 필드 설명 추가
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### CSV 업로드 형식

```csv
english,korean
apple,사과
I love you,나는 너를 사랑해
```

- 첫 줄 헤더 (`english,korean`)
- 파싱 실패 행은 건너뛰고 성공한 행만 저장, 응답에 실패 행 목록 포함

---

## 프론트엔드 (Next.js)

### 기술 스택

| 항목 | 기술 |
|------|------|
| 프레임워크 | Next.js (App Router) |
| 스타일 | Tailwind CSS |
| 폰트 | Pretendard |

### 라우팅

```
/                    → 메인 (오늘 복습할 단어 요약)
/words               → 단어 목록 (주차별 필터)
/words/upload        → CSV/엑셀 업로드
/study/flashcard     → 플래시카드 모드
/study/speedrun      → 스피드 런 모드
/study/weak          → 취약 단어 모드
/stats               → 학습 통계
```

### UI/UX 규칙

- 디자인 컨셉: 토스 스타일 플랫 디자인 (흰 배경, 카드 중심)
- 메인 컬러: `#185FA5` (블루)
- 배경: 흰색 + 연한 회색 카드
- 정답 색상: `#3B6D11` (초록)
- 오답 색상: `#A32D2D` (빨강)
- 코너 반경: `rounded-xl` (12~14px)
- 불필요한 장식 없이 핵심 정보만 표시

---

## Docker Compose

- 서비스: `frontend` / `backend` / `mariadb`
- 로컬 개발 시 `.env` 파일로 환경 변수 관리
- MariaDB 초기화 스크립트는 `db/init.sql`에 위치

---

## 개발 순서 (현재 진행 상황)

```
[ ] 1단계: DB 설계 및 MariaDB Docker 세팅
[ ] 2단계: Spring Boot 프로젝트 생성 및 백엔드 API 구현
[ ] 3단계: Next.js 프로젝트 생성 및 프론트엔드 구현
[ ] 4단계: Docker Compose 통합
[ ] 5단계: 맥미니 Ubuntu VM 배포
[ ] 6단계: Cloudflare Tunnel 연결 (minu-dev.win)
```
