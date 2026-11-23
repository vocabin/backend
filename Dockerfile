# ── Build stage ───────────────────────────────────────────────────────────────
FROM gradle:8.13-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl python3 python3-venv && rm -rf /var/lib/apt/lists/*
# Caption fetching (listening transcript). Pinned; bump this version when YouTube changes break it.
RUN python3 -m venv /opt/transcript && /opt/transcript/bin/pip install --no-cache-dir youtube-transcript-api==1.2.5
ENV TRANSCRIPT_PYTHON=/opt/transcript/bin/python
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
