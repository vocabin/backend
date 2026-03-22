# 영어 학습 웹앱 프로젝트 설계서

## 1. 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 프로젝트명 | 영어 학습 전용 복습 앱 |
| 목적 | 퀴즐렛 주차별 단어/문장을 효율적으로 복습 |
| 사용자 | 개인 전용 (싱글 유저) |
| 배포 환경 | 맥미니 홈서버 (Ubuntu VM / k8s) |
| 외부 접근 | Cloudflare Tunnel (minu-dev.win) |

---

## 2. 기술 스택

| 영역 | 기술 |
|------|------|
| 프론트엔드 | Next.js + Tailwind CSS |
| 백엔드 | Java Spring Boot |
| ORM | Spring Data JPA + Hibernate |
| API 문서 | Swagger (springdoc-openapi) |
| 데이터베이스 | MariaDB |
| 컨테이너 | Docker + Docker Compose |
| 개발 방식 | Claude Code 바이브 코딩 |

---

## 3. 핵심 기능

### 3-1. 단어/문장 관리
- CSV / 엑셀 파일 업로드로 단어 일괄 등록 (퀴즐렛 즐겨찾기 단어 전체 업로드)
- 주차별 세트 관리 (1주차, 2주차 등)
- 단어 / 문장 개별 수정, 삭제

### 3-2. 학습 모드

#### 플래시카드
- 앞면 : 영어 단어/문장
- 뒷면 : 한국어 뜻
- 알면 O / 모르면 X 로 분류
- 틀린 카드는 세션 내에서 다시 출제

#### 스피드 런
- 제한 시간 안에 최대한 많이 풀기
- 타이머 표시
- 세션 종료 후 결과 (맞은 수 / 틀린 수 / 소요 시간)

#### 취약 단어 모아보기
- 틀린 횟수가 많은 단어 자동 집계
- 취약 단어만 모아서 집중 학습 가능
- 취약 단어 플래시카드 모드 제공

#### 복습 일정 자동 추천 (분산 반복)
- 에빙하우스 망각 곡선 기반
- 마지막 학습일 + 정답률로 다음 복습일 계산
- 오늘 복습할 단어 목록 메인 화면에 표시

---

## 4. 화면 구성 (페이지)

```
/                    → 메인 (오늘 복습할 단어 요약)
/words               → 단어 목록 (주차별 필터)
/words/upload        → CSV/엑셀 업로드
/study/flashcard     → 플래시카드 모드
/study/speedrun      → 스피드 런 모드
/study/weak          → 취약 단어 모드
/stats               → 학습 통계
```

---

## 5. DB 설계

### word_set (주차별 단어 세트)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| name | VARCHAR(100) | 예: "1주차", "2주차" |
| created_at | DATETIME | |

### word (단어/문장)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| word_set_id | BIGINT FK | 주차 세트 |
| english | VARCHAR(500) | 영어 단어/문장 |
| korean | VARCHAR(500) | 한국어 뜻 |
| created_at | DATETIME | |

### study_record (학습 기록)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| word_id | BIGINT FK | |
| mode | VARCHAR(50) | flashcard / speedrun / weak |
| is_correct | BOOLEAN | 정답 여부 |
| studied_at | DATETIME | 학습 시간 |

### review_schedule (복습 일정)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| word_id | BIGINT FK | |
| next_review_at | DATE | 다음 복습 예정일 |
| interval_days | INT | 복습 간격 (일) |
| ease_factor | FLOAT | 난이도 계수 |
| updated_at | DATETIME | |

---

## 6. API 설계

### 단어 관리
| Method | URL | 설명 |
|--------|-----|------|
| GET | /api/word-sets | 세트 목록 |
| POST | /api/word-sets | 세트 생성 |
| GET | /api/word-sets/{id}/words | 단어 목록 |
| POST | /api/word-sets/{id}/upload | CSV 업로드 |
| PUT | /api/words/{id} | 단어 수정 |
| DELETE | /api/words/{id} | 단어 삭제 |

### 학습
| Method | URL | 설명 |
|--------|-----|------|
| GET | /api/study/today | 오늘 복습할 단어 |
| GET | /api/study/weak | 취약 단어 목록 |
| POST | /api/study/record | 학습 결과 저장 |

### 통계
| Method | URL | 설명 |
|--------|-----|------|
| GET | /api/stats/summary | 전체 통계 요약 |
| GET | /api/stats/word/{id} | 단어별 통계 |

### Swagger
- 의존성 : `springdoc-openapi-starter-webmvc-ui`
- 접속 URL : `http://localhost:8080/swagger-ui/index.html`
- 모든 컨트롤러에 `@Tag`, `@Operation` 어노테이션 적용
- 요청/응답 DTO에 `@Schema` 어노테이션으로 필드 설명 추가

---

## 7. 분산 반복 알고리즘 (SM-2 기반)

퀴즐렛, Anki 등에서 쓰는 알고리즘.

```
정답 → interval 늘어남 (다음에 더 늦게 복습)
오답 → interval 초기화 (내일 다시 복습)

초기값
- interval : 1일
- ease_factor : 2.5

정답 시
- interval = interval × ease_factor
- ease_factor 유지 또는 소폭 증가

오답 시
- interval = 1 (내일 다시)
- ease_factor 감소 (최소 1.3)
```

---

## 8. 개발 순서

```
1단계 : DB 설계 및 MariaDB 세팅
2단계 : Spring Boot 프로젝트 생성
         - 단어 CRUD API
         - CSV 업로드 파싱
         - 학습 기록 저장
         - 복습 일정 계산 로직
3단계 : Next.js 프로젝트 생성
         - 메인 화면
         - 단어 목록 / 업로드
         - 플래시카드 모드
         - 스피드 런 모드
         - 취약 단어 모드
4단계 : Docker Compose 구성
5단계 : 맥미니 Ubuntu VM에 배포
6단계 : Cloudflare Tunnel 연결
```

---

## 9. 배포 구조

```
minu-dev.win
     ↓ Cloudflare Tunnel
맥미니 (macOS)
└── Ubuntu VM (OrbStack)
     └── Docker Compose
          ├── Next.js 컨테이너 (프론트)
          ├── Spring Boot 컨테이너 (백엔드)
          └── MariaDB 컨테이너 (DB)
```

---

## 10. UI/UX 컨셉

### 디자인 방향
토스 스타일 — 흰 배경, 카드 중심 레이아웃, 불필요한 요소 없이 핵심만.

| 항목 | 내용 |
|------|------|
| 스타일 | 토스 스타일 플랫 디자인 |
| 메인 컬러 | #185FA5 (블루) |
| 배경 | 흰색 + 연한 회색 카드 |
| 폰트 | 시스템 폰트 (Pretendard 권장) |
| 정답 색상 | #3B6D11 (초록) |
| 오답 색상 | #A32D2D (빨강) |
| 코너 | 12~14px 라운드 |

### 화면별 UI 포인트

#### 메인 홈
- 오늘 복습할 단어 수, 정답률, 취약 단어 수를 상단 숫자 카드로 한눈에
- 주차별 세트를 카드로 나열, 각 카드에 진행률 바 표시
- 완료 / 학습중 / 시작 전 뱃지로 상태 표시

#### 플래시카드
- 카드 중앙 배치, 탭하면 뜻 공개
- 하단에 크게 "✕ 모르겠어" / "○ 알아요" 버튼
- 상단에 진행률 바 + 남은 카드 수

#### 스피드 런
- 타이머를 화면 중앙 상단에 크게 배치
- 단어 카드 + O/X 버튼 단순화
- 결과 화면에 맞은 수 / 틀린 수 / 소요 시간 표시

#### 취약 단어
- 틀린 횟수 기준 내림차순 리스트
- 각 단어 옆에 "N회 틀림" 빨간색으로 표시
- 하단에 "취약 단어만 집중 학습" 버튼

### 앱 아이콘
- 컨셉 : 다크 네이비 배경 + 블루 카드 스택 3장 겹침
- 배경색 : #1A1A2E
- 카드 색상 : #2D5BE3 (30% / 50% / 흰색)
- 중앙 텍스트 : "Aa" (영어) + "가나다" (한국어)
- 아이콘 파일 : `app_icon.svg` (1024px 기준)

---

## 11. CSV 업로드 형식

```csv
english,korean
apple,사과
I love you,나는 너를 사랑해
```

첫 줄은 헤더, 이후 한 줄에 영어/한국어 한 쌍.
