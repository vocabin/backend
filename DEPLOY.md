# 배포 가이드 (맥미니 홈서버 / OrbStack)

## 접속

```bash
ssh minu-dev.win
```

Cloudflare Tunnel을 통해 맥미니에 SSH 접속 (`~/.ssh/config`에 `ProxyCommand cloudflared access ssh` 설정됨).

## 실제 서비스가 떠 있는 곳

맥미니에는 OrbStack 머신이 여러 개 있을 수 있다. **실제 컨테이너가 떠 있는 머신은 `ubuntu`** — 다른 이름의 머신(`homeserver` 등)과 헷갈리지 않도록 주의.

```bash
orb shell -m ubuntu
```

머신 이름이 헷갈리면 컨테이너 라벨로 실제 compose 경로를 확인:

```bash
docker inspect vocabin-backend --format '{{index .Config.Labels "com.docker.compose.project.working_dir"}}'
```

## 배포 경로

```
/Users/minu/vocabin/                  ← docker-compose.yml, .env (git 미연결, 수동 관리)
/Users/minu/vocabin/backend/          ← git 저장소 (vocabin/backend, branch: main)
```

**주의**: `/Users/minu/vocabin` 최상위 폴더 자체는 git으로 관리되지 않는다. `docker-compose.yml`, `.env`는 코드 변경 시 수동으로 맞춰줘야 한다 (예: 새 환경변수가 추가되면 이 두 파일에 직접 반영 필요).

## 컨테이너 구성

| 컨테이너 | 이미지/빌드 | 포트 |
|---|---|---|
| `vocabin-mariadb` | `mariadb:11` | 3306 |
| `vocabin-backend` | `./backend` 빌드 (Dockerfile, Gradle 멀티스테이지) | 8080 |
| `vocabin-frontend` | `./frontend` 빌드 | 3000 |

`backend`, `frontend`는 `docker-compose.yml`에 `profiles: ["app"]`로 지정되어 있어 `--profile app` 옵션이 필요하다.

## 배포 절차

### 1. 백엔드 코드 최신화

```bash
cd /Users/minu/vocabin/backend
git pull origin main
```

### 2. (필요 시) `docker-compose.yml` / `.env` 수동 반영

새 환경변수가 추가된 경우, 개발 PC에서 어떤 값이 필요한지 확인 후 서버의 `/Users/minu/vocabin/docker-compose.yml`, `/Users/minu/vocabin/.env`에 직접 추가.

### 3. 이미지 재빌드 + 재기동

```bash
cd /Users/minu/vocabin
docker compose --profile app up -d --build backend    # 백엔드만
docker compose --profile app up -d --build frontend   # 프론트만
docker compose --profile app up -d --build            # 전체
```

### 4. 로그 확인

```bash
docker compose logs -f backend
```

## 트러블슈팅

- `docker ps`가 비어 보이면: 잘못된 OrbStack 머신에 있는 것일 수 있다. 맥미니 호스트 프롬프트(`orb shell` 밖)에서 `docker ps -a`로 먼저 확인.
- 컨테이너 안 docker CLI가 없으면: OrbStack의 각 Linux 머신은 독립적이라 도커 데몬이 없는 머신일 수 있다. `ubuntu` 머신인지 확인.
