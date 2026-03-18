# Car Reselling MVP

End-to-end MVP for a used car reseller in Brazil. The application includes a Java 21 + Spring Boot 3.x backend and a React 18 + TailwindCSS frontend, with MySQL as the database and Liquibase for schema migrations.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Environment Variables](#environment-variables)
- [Database](#database)
- [Build & Run](#build--run)
- [Building the Backend JAR](#building-the-backend-jar)
- [Production Stack](#production-stack)
- [CI/CD with Jenkins](#cicd-with-jenkins)
- [Observability](#observability)
- [Kubernetes](#kubernetes)
- [API Documentation](#api-documentation)
- [Debugging](#debugging)
- [Common Troubleshooting](#common-troubleshooting)
- [Commands Summary](#commands-summary)

---

## Features

- Vehicle registration, listing, and detail view
- Service management per vehicle with totals
- Document upload, download, and deletion (local storage)
- Distribution to partner dealerships
- Distributed vehicles report with totals

---

## Tech Stack

**Backend**
- Java 21, Spring Boot 3.x
- Gradle 8.7
- MySQL 8.x
- Liquibase
- Springdoc OpenAPI
- OpenTelemetry (traces, metrics, logs)
- Micrometer OTLP registry

**Frontend**
- React 18 + TypeScript
- TailwindCSS
- Vite
- Axios

**Observability**
- OpenTelemetry Collector
- Prometheus
- Grafana Tempo (traces)
- Grafana Loki (logs)
- Grafana (dashboards)

---

## Project Structure

```
.
├── backend/                      # Spring Boot API
├── frontend/                     # React + Vite UI
├── grafana/
│   └── provisioning/
│       ├── datasources/          # Auto-provisioned Grafana datasources
│       └── plugins/
├── docker-compose.yml                     # Development stack
├── docker-compose-prod.yml                # Production stack (DockerHub images)
├── docker-compose-observality.yml         # Observability stack (standalone)
├── otel-collector-config.yml              # OpenTelemetry Collector pipeline
├── prometheus.yml                         # Prometheus scrape configuration
├── tempo-config.yml                       # Grafana Tempo configuration
├── loki-config.yml                        # Grafana Loki configuration
├── kubernetes/
│   ├── observability-cluster.yml          # kind cluster: observability stack
│   ├── observability-deployment.yml       # Grafana, Loki, Tempo, Prometheus, OTel
│   ├── car-reselling-api-cluster.yml      # kind cluster: car-reselling-api
│   ├── car-reselling-api-deployment.yml   # Deployment, Service, HPA, PDB
│   ├── authentication-api-cluster.yml     # kind cluster: authentication-api
│   └── authentication-api-deployment.yml  # Deployment, Service, HPA, PDB
├── .env                                   # Environment variables (not committed)
└── .env.example                           # Template for .env
```

---

## Prerequisites

- Java 21
- Gradle 8.7 (or use Gradle wrapper if added later)
- Node.js 20+
- npm 9+
- Docker 27.5.1 / Docker Compose 2.29.2 (for containerized setup)
- MySQL 8.x (if running locally without Docker)

---

## Configuration

### Backend

Default settings are in `backend/src/main/resources/application.yml`:

- Server: `http://localhost:8080`
- MySQL:
  - DB: `car_reselling`
  - User: `car`
  - Password: `car`
- File storage: `/storage/vehicles`
- Multipart file limit: 20MB

You can override datasource settings with environment variables:

```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/car_reselling
SPRING_DATASOURCE_USERNAME=car
SPRING_DATASOURCE_PASSWORD=car
```

#### HTTPS (Let's Encrypt)

1) Obtain a certificate with Certbot (example for Nginx/standalone):

```
sudo certbot certonly --standalone -d your-domain.com
```

2) Convert the certificate to PKCS12 for Spring Boot:

```
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/your-domain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/your-domain.com/privkey.pem \
  -out /etc/letsencrypt/live/your-domain.com/keystore.p12 \
  -name springboot
```

3) Configure Spring Boot SSL via environment variables:

```
SERVER_PORT=443
SERVER_SSL_ENABLED=true
SERVER_SSL_KEY_STORE=file:/etc/letsencrypt/live/your-domain.com/keystore.p12
SERVER_SSL_KEY_STORE_PASSWORD=yourpassword
SERVER_SSL_KEY_STORE_TYPE=PKCS12
SERVER_SSL_KEY_ALIAS=springboot
```

4) Start the backend with those env vars set.

Docker note: mount the Let's Encrypt folder into the container and point
`SERVER_SSL_KEY_STORE` to the mounted file path.

Example docker compose override:

```yaml
services:
  car-reselling-api:
    volumes:
      - /etc/letsencrypt:/etc/letsencrypt:ro
    environment:
      SERVER_PORT: 443
      SERVER_SSL_ENABLED: "true"
      SERVER_SSL_KEY_STORE: "file:/etc/letsencrypt/live/your-domain.com/keystore.p12"
      SERVER_SSL_KEY_STORE_PASSWORD: "yourpassword"
      SERVER_SSL_KEY_STORE_TYPE: "PKCS12"
      SERVER_SSL_KEY_ALIAS: "springboot"
```

### Frontend

Vite dev server runs on `http://localhost:5173` and proxies `/api` to `http://localhost:8080`.

---

## Database

### Schema & Migrations

Liquibase migrations are stored at:

```
backend/src/main/resources/db/changelog/db.changelog-master.yaml
```

Tables:

- `vehicles`
- `services`
- `documents`
- `partners`

### MySQL via Docker Compose

Start only MySQL:

```
docker compose up -d mysql
```

Connect to MySQL and run a query on `vehicles`:

```
docker compose exec mysql mysql -u car -p car_reselling
SELECT id, license_plate, status FROM vehicles LIMIT 10;
```

Connect to MySQL and run an update on `vehicles`:

```
docker compose exec mysql mysql -u car -p car_reselling
UPDATE vehicles SET status = 'SOLD' WHERE license_plate = 'ABC1234';
```

Show all database users:

```
SELECT user, host FROM mysql.user;
```

Show all tables:

```
docker compose exec mysql mysql -u car -p car_reselling
SHOW TABLES;
```

Show all tables (single command with `docker exec`):

```
docker exec -it mysql mysql -u car -pcar -D car_reselling -e "SHOW TABLES;"
```

Show all tables filtering by name:

```
docker compose exec mysql mysql -u car -p car_reselling
SHOW TABLES LIKE '%vehicle%';
```

Show all columns from a specific table:

```
docker compose exec mysql mysql -u car -p car_reselling
SHOW COLUMNS FROM vehicles;
```

Show all indexes from a specific table:

```
docker compose exec mysql mysql -u car -p car_reselling
SHOW INDEX FROM vehicles;
```

### Seed Data

The changelog seeds:

- Partner A (Sao Paulo)
- Partner B (Rio de Janeiro)
- One example vehicle

### Storage

Documents are stored locally under `/storage/vehicles/{vehicleId}/{documentId}/{filename}`.
When using Docker, the `./storage` folder is mounted to `/storage` in the container.

---

## Build & Run

### Option A — Docker (recommended)

Build and run everything:

```
docker compose up --build
```

Then open:

- App: `http://localhost:5173`
- API: `http://localhost:8080/api/v1`
- Swagger UI (main): `http://localhost:8080/swagger-ui/index.html`
- Swagger UI (auth): `http://localhost:8081/swagger-ui/index.html`

Stop:

```
docker compose down
```

List running containers with a clean summary:

```
docker ps --format "table {{.ID}}\t{{.Image}}\t{{.Status}}\t{{.Names}}"
```

Checking all docker image size:
```
docker images --format='{{.Repository}}:{{.Tag}}\t{{.Size}}'
```

# Replace <image_name> with the repository:tag or Image ID


### Option B — Local development

#### 1) Start MySQL

Use your local MySQL or Docker:

```
docker run --name car-mysql -e MYSQL_DATABASE=car_reselling -e MYSQL_USER=car -e MYSQL_PASSWORD=car -e MYSQL_ROOT_PASSWORD=car -p 3306:3306 -d mysql:8.0
```

#### 2) Backend

```
cd backend
./gradlew bootRun
```

Backend runs at `http://localhost:8080`.

#### 3) Frontend

```
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173` (proxying `/api` to the backend).

---

## Environment Variables

All runtime configuration lives in a single `.env` file at the project root.  
`.env` is git-ignored; use `.env.example` as the starting template.

### Creating the `.env` file

```bash
cp .env.example .env
# Edit .env and replace every "change-me" value with real credentials
```

### Managing environment variables — `load-env.sh`

`load-env.sh` exports variables from a `.env` file (or any env file) in three
different scopes. The script auto-detects your shell (`$SHELL`) and operates on
the correct RC file (`~/.bashrc`, `~/.zshrc`, etc.).

| Command | Scope | Persists? |
|---|---|---|
| `source ./load-env.sh` | Current terminal only | Until terminal closes |
| `source ./load-env.sh --global` | All new terminals | Yes — written to RC file |
| `./load-env.sh --remove-global` | Removes from RC file | — |
| `./load-env.sh --status` | Diagnostic info | — |
| `source ./load-env.sh --unset-session` | Unset from current terminal | — |

---

#### Session scope — current terminal only

> **Must be `source`d** so variables live in the current shell, not a child process.

```bash
# Load .env (default)
source ./load-env.sh

# Shorter dot syntax
. ./load-env.sh

# Load a different file
source ./load-env.sh .env.development
```

Output:

```
load-env [session]: exported 30 variable(s) from .env
```

---

#### Global scope — persist across all terminals

Adds a single `source` line to your RC file so every new terminal automatically
loads the variables on startup.

> `--global` can be **sourced or executed** — it also exports into the current session immediately.

```bash
# Persist .env globally
source ./load-env.sh --global

# Persist a different file
source ./load-env.sh --global .env.development
```

Output:

```
load-env [global]: added source line to /home/you/.bashrc
load-env [global]: exported 30 variable(s) to current session

  New terminals will load variables automatically.
  To apply in existing open terminals run:
    source /home/you/.bashrc
```

The line written to the RC file looks like:

```bash
source "/abs/path/to/load-env.sh" "/abs/path/to/.env"  # car-reselling:/abs/path/.env
```

This is one clean line per env file. Running `--global` again safely **replaces** that line instead of duplicating it. Because the RC file just delegates to `load-env.sh`, any future edits to `.env` are automatically picked up in new terminals — no need to re-run `--global`.

---

#### Remove global variables from the RC file

```bash
# Remove .env entry
./load-env.sh --remove-global

# Remove a specific file's entry
./load-env.sh --remove-global .env.development
```

Output:

```
load-env [remove-global]: removed entry for '.env' from /home/you/.bashrc

  Variables are still active in open terminals.
  To unset them in the current session:
    source ./load-env.sh --unset-session
```

---

#### Unset variables from the current session

```bash
source ./load-env.sh --unset-session

# Unset from a specific file
source ./load-env.sh --unset-session .env.development
```

---

#### Status — inspect what is set where

```bash
./load-env.sh --status
```

Output:

```
load-env [status]
  env file : /home/you/projects/car-reselling/.env
  RC file  : /home/you/.bashrc
  global   : ✓ entry is present in /home/you/.bashrc
  source line:
    source "/home/you/projects/car-reselling/load-env.sh" ".../.env"  # car-reselling:...

  VARIABLE                                 IN SESSION
  --------                                 ----------
  DOCKERHUB_USERNAME                       yes
  AUTH_BASE_URL                            yes
  SPRING_DATASOURCE_URL                    no
  ...
```

---

#### Verify a specific variable

```bash
echo $AUTH_BASE_URL
printenv SPRING_DATASOURCE_URL
```

---

#### Typical workflows

**Option A — session only (no permanent changes)**

```bash
# 1. Start MySQL in Docker
docker compose up -d mysql

# 2. Load vars into this terminal only
source ./load-env.sh

# 3. Run the backend in the same terminal
cd backend && ./gradlew bootRun
```

**Option B — global (set once, works in every new terminal)**

```bash
# Run once — variables will be available in all future terminals
source ./load-env.sh --global

# Any new terminal: variables are already loaded
cd backend && ./gradlew bootRun
```

**Cleaning up**

```bash
# 1. Remove from RC file (stops loading in new terminals)
./load-env.sh --remove-global

# 2. Also unset from the current session
source ./load-env.sh --unset-session
```

---

> **Shell compatibility**: requires **Bash 4+** or **Zsh**.  
> On macOS the system `/bin/bash` is 3.x — install a newer Bash (`brew install bash`) or use Zsh (`zsh`), both are supported.

---

## Building the Backend JAR

The `build-api.sh` script wraps `./gradlew clean build` and lets you stamp a specific version onto the JAR from the command line without editing `build.gradle`.

### Usage

```bash
./build-api.sh [OPTIONS] [VERSION]
```

| Argument / Option | Description |
|---|---|
| `VERSION` | Semantic version string (e.g. `1.8` or `2.0.1`). Omit to use the default declared in `build.gradle`. |
| `-s`, `--skip-tests` | Skip the test phase (passes `-x test` to Gradle). |
| `-h`, `--help` | Print usage information and exit. |

### Examples

```bash
# Build with the default version defined in build.gradle
./build-api.sh

# Build and stamp the JAR as car-reselling-api-1.8.jar
./build-api.sh 1.8

# Build version 1.8 without running tests
./build-api.sh --skip-tests 1.8
./build-api.sh -s 1.8

# Print help
./build-api.sh --help
```

### Output

A successful build prints:

```
==============================================
  Build successful!
  Artifact : backend/build/libs/car-reselling-api-1.8.jar
  Size     : 69M
==============================================
```

The JAR is placed at:

```
backend/build/libs/car-reselling-api-<VERSION>.jar
```

### How versioning works

`build.gradle` reads the version from the Gradle property `projectVersion` when it is supplied, and falls back to its hardcoded default otherwise:

```groovy
version = findProperty('projectVersion') ?: '1.6'
```

The script passes `-PprojectVersion=<VERSION>` to Gradle when a version argument is given, so the JAR name, the `bootJar` manifest, and the Spring Boot `/actuator/info` endpoint all reflect the version you specified.

---

## Production Stack

The production stack pulls images from DockerHub that were built and pushed by Jenkins.

### Setup

1. Copy `.env.example` to `.env` and fill in your values:

```
cp .env.example .env
```

Key variables to set:

| Variable | Description |
|---|---|
| `DOCKERHUB_USERNAME` | Your Docker Hub username |
| `API_VERSION` | Tag of the `car-reselling-api` image to deploy |
| `UI_VERSION` | Tag of the `car-reselling-ui` image to deploy |
| `AUTH_VERSION` | Tag of the `authentication-api` image to deploy |
| `MYSQL_ROOT_PASSWORD` | MySQL root password |
| `MYSQL_PASSWORD` | Password for the `car` MySQL user |

2. Start the production stack:

```
docker compose -f docker-compose-prod.yml up -d
```

3. To run the application together with the full observability stack, first create the shared network, then start both files together:

```
docker network create car-reselling-net
docker compose -f docker-compose-prod.yml -f docker-compose-observality.yml up -d
```

### Services exposed

| URL | Service |
|---|---|
| `http://localhost:5173` | Frontend (also on port 80) |
| `http://localhost:8080` | Main backend API |
| `http://localhost:8081` | Authentication API |
| `http://localhost:3000` | Grafana |
| `http://localhost:9090` | Prometheus |

---

## CI/CD with Jenkins

Jenkins pipelines are defined as `Jenkinsfile` in each service directory.

| File | Builds |
|---|---|
| `backend/Jenkinsfile` | `car-reselling-api` Docker image |
| `frontend/Jenkinsfile` | `car-reselling-ui` Docker image |

### Pipeline stages

Both pipelines follow the same pattern:

1. **Checkout** — clone the repository from GitHub
2. **Install** — install dependencies inside a Docker container (`gradle` / `node:20-alpine`)
3. **Build** — compile / bundle the application
4. **Dependency Validation** — Trivy CVE scan of dependencies
5. **Type Check** — `tsc --noEmit` (frontend) / Gradle compile check (backend)
6. **Package** — archive the build artifact
7. **Docker Build & Tag** — build the Docker image; tag with `VERSION` parameter or `{build}-{git-sha}` fallback
8. **Docker Push** — push the tagged image to Docker Hub using the `dockerhub-creds` Jenkins credential

### Required Jenkins credentials

Create a credential with ID `dockerhub-creds` of type **Username with Password**:
- Username: your Docker Hub username (not email)
- Password: a Docker Hub access token

### Triggering a build

Use the `VERSION` parameter (e.g. `1.2.0`) to produce a predictable image tag. Leave it blank to get an auto-generated `{buildNumber}-{gitSha}` tag.

---

## Observability

The project ships a complete observability stack based on the **OpenTelemetry** standard. All signals (traces, metrics, logs) flow through a single collector and are stored in dedicated backends, all visualised in Grafana.

### Architecture overview

```
┌──────────────────────────────┐
│       car-reselling-api      │
│  (Spring Boot / Micrometer)  │
│                              │
│  ┌──────────┐ ┌───────────┐  │
│  │  Traces  │ │  Metrics  │  │  Loki4j appender
│  │  (OTLP)  │ │  (OTLP)   │  │  ──────────────►  Logs (OTLP)
│  └────┬─────┘ └─────┬─────┘  │
└───────┼─────────────┼────────┘
        │             │
        ▼             ▼
┌───────────────────────────┐
│      otel-collector        │
│  gRPC :4317  HTTP :4318    │
│                            │
│  receivers: otlp           │
│  processors: batch         │
│  exporters:                │
│    logs   → Loki           │
│    traces → Tempo          │
│    metrics→ Prometheus     │
└──────┬──────┬──────┬───────┘
       │      │      │
       ▼      ▼      ▼
    Loki   Tempo  Prometheus
    :3100  :3200   :9090
       │      │      │
       └──────┴──────┘
                │
            Grafana :3000
```

### Starting the observability stack

The observability stack is defined in `docker-compose-observality.yml` and is **independent** from the application stack. It can be started alongside either the development or production compose file.

**With the development stack:**

```bash
# Create the shared network once
docker network create car-reselling-net

# Start both stacks together
docker compose -f docker-compose.yml -f docker-compose-observality.yml up -d
```

**With the production stack:**

```bash
docker network create car-reselling-net
docker compose -f docker-compose-prod.yml -f docker-compose-observality.yml up -d
```

**Standalone (just the observability tools):**

```bash
docker compose -f docker-compose-observality.yml up -d
```

> **Why the shared network?**
> Each Compose file creates its own default bridge network. Services in different files cannot reach each other by container name unless they share a network. `car-reselling-net` is declared as `external: true` in both files so that `car-reselling-api` can resolve `otel-collector`, `loki`, and `tempo` by name.

### `docker-compose-observality.yml` — service by service

#### Grafana

```yaml
grafana:
  image: grafana/grafana:12.4.0-...
  ports:
    - "3000:3000"
  volumes:
    - grafana-data:/var/lib/grafana
    - ./grafana/provisioning:/etc/grafana/provisioning:ro
```

Grafana is the single UI for all three signal types. It is provisioned automatically on first start:

- **Datasources** (`grafana/provisioning/datasources/datasources.yml`): Loki, Tempo, and Prometheus are registered so you can query them immediately without any manual setup.
- **Default credentials**: `admin` / `admin` (change in production via `GF_SECURITY_ADMIN_PASSWORD`).
- Grafana data (dashboards, users, saved queries) is persisted in the named volume `grafana-data`.

Open Grafana: `http://localhost:3000`

#### Loki

```yaml
loki:
  image: grafana/loki:3.2.0
  ports:
    - "3100:3100"
  volumes:
    - ./loki-config.yml:/etc/loki/local-config.yaml:ro
    - /tmp/observability/loki:/loki
```

Loki is the log aggregation backend. The OTel Collector pushes log records here via its `loki` exporter. Logs are then queryable in Grafana using **LogQL**.

**Configuration highlights (`loki-config.yml`):**

| Setting | Value | Purpose |
|---|---|---|
| `auth_enabled` | `false` | Single-tenant mode — no token required |
| `http_listen_port` | `3100` | API and push endpoint |
| `schema_config.store` | `tsdb` | Modern index format (v13) |
| `limits_config.retention_period` | `180h` (~7 days) | Automatic log expiry |
| `compactor.compaction_interval` | `10m` | How often old chunks are compacted and deleted |
| `common.replication_factor` | `1` | Single-node mode — no replication |
| Storage paths | `/loki/chunks`, `/loki/tsdb-*` | All data written inside the bind-mount `/tmp/observability/loki` |

Logs are correlated with traces via the **trace ID** injected into each log record by the backend's MDC (Mapped Diagnostic Context). In Grafana you can jump from a log line directly to its trace in Tempo.

#### Tempo

```yaml
tempo:
  image: grafana/tempo:2.6.1
  ports:
    - "3200:3200"   # HTTP query API (Grafana datasource)
    - "4319:4317"   # OTLP gRPC (host access, optional)
    - "4320:4318"   # OTLP HTTP (host access, optional)
  volumes:
    - ./tempo-config.yml:/etc/tempo/config.yml:ro
    - /tmp/observability/tempo:/tmp/tempo
```

Tempo stores distributed traces. The OTel Collector forwards spans to Tempo via OTLP gRPC on port `4317` (internal network only).

**Configuration highlights (`tempo-config.yml`):**

| Setting | Value | Purpose |
|---|---|---|
| `http_listen_port` | `3200` | Query API used by Grafana |
| `stream_over_http_enabled` | `true` | Enables streaming responses for large trace queries |
| `distributor.receivers.otlp` | gRPC `:4317`, HTTP `:4318` | Accepts spans from the OTel Collector |
| `ingester.max_block_duration` | `5m` | Flushes in-memory spans to disk every 5 minutes |
| `compactor.block_retention` | `180h` (~7 days) | Traces are kept for 7 days then pruned |
| `storage.trace.backend` | `local` | Single-node file storage under `/tmp/tempo` |
| `metrics_generator.processors` | `service-graphs`, `span-metrics` | Derives RED metrics (Rate, Errors, Duration) from traces and pushes them to Prometheus via remote-write |

The `metrics_generator` is a powerful feature: Tempo analyses the trace spans it ingests and automatically generates service-graph metrics and span-latency histograms, which it then pushes directly to Prometheus. This means you get infrastructure-level metrics for free from your traces without any extra instrumentation.

#### Prometheus

```yaml
prometheus:
  image: prom/prometheus:v2.54.1
  ports:
    - "9090:9090"
  command:
    - "--config.file=/etc/prometheus/prometheus.yml"
    - "--web.enable-remote-write-receiver"
    - "--storage.tsdb.retention.time=15d"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
    - /tmp/observability/prometheus:/prometheus
```

Prometheus stores time-series metrics. It scrapes two targets and also accepts remote-write from Tempo's metrics generator.

**`--web.enable-remote-write-receiver`** is required to allow Tempo's `metrics_generator` to push span-derived metrics directly into Prometheus.

**Configuration highlights (`prometheus.yml`):**

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:

  - job_name: "prometheus"
    static_configs:
      - targets: ["localhost:9090"]      # Prometheus self-monitoring

  - job_name: "otel-collector"
    static_configs:
      - targets: ["otel-collector:8888"] # Collector pipeline health metrics

  - job_name: "otel-collector-app-metrics"
    honor_labels: true                   # ← important: see below
    static_configs:
      - targets: ["otel-collector:8889"] # Application metrics exposed by the collector
```

**Why `honor_labels: true`?** When the OTel SDK sets resource attributes such as `service.name=car-reselling-api`, the Collector re-exposes them as Prometheus labels (including `job`). Without `honor_labels: true`, Prometheus would overwrite the `job` label with `otel-collector-app-metrics`. With it, the original `job="car-reselling-api"` is preserved, so PromQL queries like `{job="car-reselling-api"}` work exactly as expected.

Open Prometheus: `http://localhost:9090`

#### OpenTelemetry Collector

```yaml
otel-collector:
  image: otel/opentelemetry-collector-contrib:0.96.0
  command: ["--config=/etc/otelcol/config.yml"]
  ports:
    - "4317:4317"  # OTLP gRPC receiver  ← backend app pushes traces/metrics here
    - "4318:4318"  # OTLP HTTP receiver
    - "8888:8888"  # Collector self-telemetry (scraped by Prometheus)
    - "8889:8889"  # Prometheus exporter for application metrics
```

The OTel Collector is the central hub. It receives all telemetry from the backend, processes it, and fans it out to the appropriate backends.

**Pipeline configuration (`otel-collector-config.yml`):**

```
receivers:
  otlp:                       ← accepts from any OTLP-compatible source
    protocols:
      grpc: 0.0.0.0:4317
      http: 0.0.0.0:4318

processors:
  batch:                      ← buffers and batches signals for efficiency

exporters:
  loki:                       ← logs  → http://loki:3100/loki/api/v1/push
  otlp/tempo:                 ← traces→ tempo:4317 (gRPC, insecure)
  prometheus:                 ← metrics exposed on 0.0.0.0:8889 for Prometheus to scrape
  debug:                      ← prints detailed telemetry to container stdout (dev aid)

service:
  pipelines:
    logs:    receivers[otlp] → processors[batch] → exporters[loki, debug]
    traces:  receivers[otlp] → processors[batch] → exporters[otlp/tempo]
    metrics: receivers[otlp] → processors[batch] → exporters[prometheus]
```

The `debug` exporter on the logs pipeline prints every log record to the container's stdout. This is useful during development to confirm the pipeline is working. It can be removed in production by deleting `debug` from the `logs.exporters` list.

### Backend instrumentation

The backend is instrumented via two independent mechanisms — both push to the OTel Collector:

**1. Distributed tracing (OpenTelemetry SDK)**

Controlled by `observability.tracing.enabled` in `application.yml` (defaults to `true` in Docker via `OBSERVABILITY_TRACING_ENABLED`).

```yaml
observability:
  tracing:
    enabled: ${OBSERVABILITY_TRACING_ENABLED:false}
    otlp:
      endpoint: ${OBSERVABILITY_OTLP_ENDPOINT:http://localhost:4317}
```

When enabled, every incoming HTTP request is automatically wrapped in a span and forwarded to `otel-collector:4317` via OTLP gRPC. Trace IDs are injected into the MDC so that every log line emitted during a request carries the same `traceId` and `spanId`.

**2. Metrics (Micrometer OTLP push)**

Controlled by `management.otlp.metrics.export.enabled` (defaults to `true` in Docker via `OTLP_METRICS_ENABLED`).

```yaml
management:
  otlp:
    metrics:
      export:
        enabled: ${OTLP_METRICS_ENABLED:false}
        url: ${OTLP_METRICS_URL:http://otel-collector:4318/v1/metrics}
        step: 30s
        resource-attributes:
          service.name: car-reselling-api
```

Micrometer pushes a full set of JVM, HTTP, HikariCP (connection pool), and custom application metrics every 30 seconds to the OTel Collector over OTLP HTTP. The `service.name` resource attribute is carried all the way through to Prometheus, enabling label-based filtering by service.

**Environment variables that control observability:**

| Variable | Default (Docker) | Purpose |
|---|---|---|
| `OBSERVABILITY_TRACING_ENABLED` | `true` | Enable/disable trace export |
| `OBSERVABILITY_OTLP_ENDPOINT` | `http://otel-collector:4317` | Collector gRPC endpoint for traces |
| `OTLP_METRICS_ENABLED` | `true` | Enable/disable metric push |
| `OTLP_METRICS_URL` | `http://otel-collector:4318/v1/metrics` | Collector HTTP endpoint for metrics |
| `LOKI_URL` | `http://loki:3100` | Loki endpoint for the Loki4j log appender |

### Grafana datasources (auto-provisioned)

Defined in `grafana/provisioning/datasources/datasources.yml`:

| Datasource | UID | URL | Default |
|---|---|---|---|
| Prometheus | `prometheus` | `http://prometheus:9090` | No |
| Tempo | `tempo` | `http://tempo:3200` | Yes |
| Loki | `loki` | `http://loki:3100` | No |

All three datasources are automatically available the first time Grafana starts. No manual configuration is needed.

### Useful Grafana queries

**Explore logs (LogQL):**
```logql
{service_name="car-reselling-api"} |= "ERROR"
```

**Find a trace by ID (Tempo):**
Enter the trace ID directly in the Tempo datasource Explorer.

**Query application metrics (PromQL):**
```promql
# HTTP request rate
rate(http_server_requests_seconds_count{job="car-reselling-api"}[1m])

# 95th-percentile response time
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job="car-reselling-api"}[5m]))

# HikariCP active connections
hikaricp_connections_active{job="car-reselling-api"}

# JVM heap used
jvm_memory_used_bytes{area="heap", job="car-reselling-api"}
```

### Ports reference

| Port | Service | Purpose |
|---|---|---|
| `3000` | Grafana | Dashboard UI |
| `3100` | Loki | Log push & query API |
| `3200` | Tempo | Trace query API |
| `4317` | OTel Collector | OTLP gRPC receiver (app → collector) |
| `4318` | OTel Collector | OTLP HTTP receiver |
| `4319` | Tempo | OTLP gRPC (host-to-Tempo, optional) |
| `4320` | Tempo | OTLP HTTP (host-to-Tempo, optional) |
| `8888` | OTel Collector | Self-telemetry (scraped by Prometheus) |
| `8889` | OTel Collector | App metrics Prometheus exporter |
| `9090` | Prometheus | Metrics query UI & API |

---

## Kubernetes

The `kubernetes/` folder contains everything needed to run the full application stack on local kind clusters.

```
kubernetes/
├── observability-cluster.yml          # kind cluster: observability stack (Grafana, Loki, Tempo, Prometheus, OTel)
├── observability-deployment.yml       # Manifests for the observability stack
├── car-reselling-api-cluster.yml      # kind cluster: car-reselling-api (NodePort 30808 → localhost:8080)
├── car-reselling-api-deployment.yml   # Manifests: Namespace, ConfigMap, Secret, Deployment, Service, Ingress, HPA, PDB
├── authentication-api-cluster.yml     # kind cluster: authentication-api (NodePort 30801 → localhost:8081)
└── authentication-api-deployment.yml  # Manifests: Namespace, ConfigMap, Secret, Deployment, Service, Ingress, HPA, PDB
```

> Each deployment file contains detailed comments at the top describing every resource it creates.

---

### Architecture — three separate clusters

The stack runs across **three independent kind clusters** on the same machine. Each cluster is a Docker container; they share the same Docker bridge network (`kind`) and communicate with each other via the host gateway IP.

```
Host machine
├── kind cluster "observability"     → localhost:3000  (Grafana)
│   namespace: observability             localhost:9090  (Prometheus)
│   Grafana, Loki, Tempo,                localhost:3100  (Loki)
│   Prometheus, OTel Collector           localhost:4317/4318 (OTel gRPC/HTTP)
│
├── kind cluster "car-reselling"     → localhost:8080  (car-reselling-api)
│   namespace: car-reselling
│   car-reselling-api
│
└── kind cluster "authentication"    → localhost:8081  (authentication-api)
    namespace: authentication
    authentication-api
```

**Cross-cluster networking:** `*.svc.cluster.local` DNS only resolves within the same cluster. To reach services in another cluster, pods use the Docker bridge **host gateway IP** (`172.22.0.1`) and the `hostPort` values mapped by `extraPortMappings` in the cluster config files.

```bash
# Find the host gateway IP (run this if it ever changes):
docker network inspect kind --format='{{range .IPAM.Config}}{{.Gateway}}{{end}}'
```

---

### Prerequisites

| Tool | Purpose | Install |
|---|---|---|
| `kubectl` | Kubernetes CLI | [docs.kubernetes.io](https://kubernetes.io/docs/tasks/tools/) |
| `kind` | Local clusters in Docker | [kind.sigs.k8s.io](https://kind.sigs.k8s.io/docs/user/quick-start/) |
| `helm` | Package manager for k8s add-ons | [helm.sh](https://helm.sh/docs/intro/install/) |

Verify your tools:

```bash
kubectl version --client
kind version
helm version
```

---

### External MySQL

MySQL runs as a plain Docker container **outside** all Kind clusters. Both APIs connect to it via the host gateway IP.

```bash
# Start external MySQL (run once)
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=car_reselling \
  -e MYSQL_USER=car \
  -e MYSQL_PASSWORD=car \
  -p 3306:3306 \
  mysql:8.0

# Verify it is accepting connections
docker exec mysql mysqladmin ping -h localhost -u root -proot --silent
```

The DB URL used by both deployments is already configured to `172.22.0.1:3306` in their Secrets.

---

### 1. Context management

Every `kubectl` command targets the **active context**. Always verify which cluster you are talking to before running `apply` or `delete`.

```bash
# Check the active context
kubectl config current-context

# List all available contexts
kubectl config get-contexts

# Switch context
kubectl config use-context kind-observability
kubectl config use-context kind-car-reselling
kubectl config use-context kind-authentication

# Or pass --context per command without switching globally
kubectl get pods -n car-reselling --context kind-car-reselling
kubectl get pods -n authentication --context kind-authentication
```

---

### 2. Create the clusters

Each cluster maps specific container ports to host ports so services are reachable at `localhost:<port>`.

```bash
# Observability stack (Grafana, Prometheus, Loki, Tempo, OTel)
kind create cluster \
  --config kubernetes/observability-cluster.yml \
  --name observability

# Car Reselling API  (nodePort 30808 → localhost:8080)
kind create cluster \
  --config kubernetes/car-reselling-api-cluster.yml \
  --name car-reselling

# Authentication API  (nodePort 30801 → localhost:8081)
kind create cluster \
  --config kubernetes/authentication-api-cluster.yml \
  --name authentication

# List all running clusters
kind get clusters
```

> **Memory note:** Each cluster node requires ~700 MB–1 GB of RAM. With all three running simultaneously you need at least 4 GB free. If cluster creation fails with `could not find a log line that matches "Reached target Multi-User System"`, you are out of memory — stop an unused cluster first:
> ```bash
> docker stop observability-control-plane   # pause without deleting
> docker start observability-control-plane  # resume later
> ```

---

### 3. Deploy the observability stack

```bash
# Switch to the observability cluster
kubectl config use-context kind-observability

# Deploy all observability resources
kubectl apply -f kubernetes/observability-deployment.yml

# Watch pods come up
kubectl get pods -n observability -w
```

Once all pods are `Running`, the following are accessible on localhost:

| URL | Service |
|---|---|
| `http://localhost:3000` | Grafana (admin / admin) |
| `http://localhost:9090` | Prometheus |
| `http://localhost:3100` | Loki push/query API |
| `http://localhost:3200` | Tempo query API |
| `localhost:4317` | OTel Collector — OTLP gRPC |
| `http://localhost:4318` | OTel Collector — OTLP HTTP |

---

### 4. Deploy car-reselling-api

```bash
# Switch to the car-reselling cluster
kubectl config use-context kind-car-reselling

# Load the local image into the cluster nodes
kind load docker-image viniciussf/car-reselling-api:latest --name car-reselling

# Apply all resources
kubectl apply -f kubernetes/car-reselling-api-deployment.yml

# Watch the pod come up
kubectl get pods -n car-reselling -w
```

Resources created in the `car-reselling` namespace:

| Resource | Name | Purpose |
|---|---|---|
| `ConfigMap` | `car-reselling-api-config` | Non-sensitive env vars (Loki URL, OTel URL, etc.) |
| `Secret` | `car-reselling-api-secret` | DB credentials |
| `Deployment` | `car-reselling-api` | Runs the API container |
| `Service` | `car-reselling-api` | NodePort 30808 → localhost:8080 |
| `Ingress` | `car-reselling-api-ingress` | NGINX EWMA load balancing |
| `HorizontalPodAutoscaler` | `car-reselling-api-hpa` | Scales 1 → 3 pods on CPU ≥ 3% or latency > 11% at 300ms |
| `PodDisruptionBudget` | `car-reselling-api-pdb` | Keeps ≥ 1 pod during disruptions |

Open: `http://localhost:8080/swagger-ui/index.html`

---

### 5. Deploy authentication-api

```bash
# Switch to the authentication cluster
kubectl config use-context kind-authentication

# Load the local image into the cluster nodes
kind load docker-image viniciussf/authentication-api:latest --name authentication

# Apply all resources
kubectl apply -f kubernetes/authentication-api-deployment.yml

# Watch the pod come up
kubectl get pods -n authentication -w
```

Resources created in the `authentication` namespace:

| Resource | Name | Purpose |
|---|---|---|
| `ConfigMap` | `authentication-api-config` | Non-sensitive env vars |
| `Secret` | `authentication-api-secret` | DB credentials + JWT_SECRET |
| `Deployment` | `authentication-api` | Runs the API container |
| `Service` | `authentication-api` | NodePort 30801 → localhost:8081 |
| `Ingress` | `authentication-api-ingress` | NGINX EWMA load balancing |
| `HorizontalPodAutoscaler` | `authentication-api-hpa` | Scales 1 → 3 pods on CPU or latency |
| `PodDisruptionBudget` | `authentication-api-pdb` | Keeps ≥ 1 pod during disruptions |

Open: `http://localhost:8081/swagger-ui/index.html`

---

### 6. Verify deployments

```bash
# ── All pods across all namespaces (all clusters need --context) ──
kubectl get pods -A -o wide --context kind-observability
kubectl get pods -A -o wide --context kind-car-reselling
kubectl get pods -A -o wide --context kind-authentication

# ── Pod details (events, probe results, image pull status) ────────
kubectl describe pod -l app=car-reselling-api -n car-reselling
kubectl describe pod -l app=authentication-api -n authentication

# ── Logs ──────────────────────────────────────────────────────────
kubectl logs -f -l app=car-reselling-api  -n car-reselling  --tail=-1
kubectl logs -f -l app=authentication-api -n authentication --tail=-1
kubectl logs -f -l app=grafana            -n observability  --tail=-1

# ── Logs from a previously crashed pod ───────────────────────────
kubectl logs -l app=car-reselling-api  -n car-reselling  --previous
kubectl logs -l app=authentication-api -n authentication --previous

# ── Check env vars actually injected into a running pod ──────────
kubectl exec -n car-reselling  deployment/car-reselling-api  -- env | grep -E "DB_URL|LOKI|OTLP"
kubectl exec -n authentication deployment/authentication-api -- env | grep -E "DB_URL|JWT"

# ── HPA status ────────────────────────────────────────────────────
kubectl get hpa -n car-reselling
kubectl describe hpa car-reselling-api-hpa -n car-reselling

# ── Services & NodePort mapping ───────────────────────────────────
kubectl get svc -n car-reselling
kubectl get svc -n authentication
```

---

### 7. Useful kubectl operations

```bash
# ── Restart a deployment (picks up ConfigMap/Secret changes) ──────
kubectl rollout restart deployment/car-reselling-api  -n car-reselling
kubectl rollout restart deployment/authentication-api -n authentication
kubectl rollout restart deployment/grafana            -n observability

# ── Wait for rollout to complete ──────────────────────────────────
kubectl rollout status deployment/car-reselling-api -n car-reselling

# ── Rollback to the previous image version ────────────────────────
kubectl rollout undo deployment/car-reselling-api -n car-reselling

# ── Manual scale (HPA overrides this at next evaluation) ─────────
kubectl scale deployment car-reselling-api --replicas=2 -n car-reselling

# ── Delete a single pod (Deployment recreates it immediately) ─────
kubectl delete pod -l app=car-reselling-api  -n car-reselling
kubectl delete pod -l app=authentication-api -n authentication

# ── Delete and recreate a stuck Service (e.g. nodePort conflict) ──
kubectl delete svc car-reselling-api -n car-reselling
kubectl apply  -f kubernetes/car-reselling-api-deployment.yml

# ── Shell into a running pod ──────────────────────────────────────
kubectl exec -it deployment/car-reselling-api  -n car-reselling  -- sh
kubectl exec -it deployment/authentication-api -n authentication -- sh

# ── Check cluster resource usage ─────────────────────────────────
kubectl top nodes
kubectl top pods -n car-reselling
```

---

### 8. Tear down

```bash
# ── Remove resources only (keeps cluster and its nodes) ───────────
kubectl delete -f kubernetes/observability-deployment.yml    --context kind-observability
kubectl delete -f kubernetes/car-reselling-api-deployment.yml  --context kind-car-reselling
kubectl delete -f kubernetes/authentication-api-deployment.yml --context kind-authentication

# ── Delete entire clusters ────────────────────────────────────────
kind delete cluster --name observability
kind delete cluster --name car-reselling
kind delete cluster --name authentication

# ── Stop external MySQL container ─────────────────────────────────
docker stop mysql
docker rm   mysql
```

---

## API Documentation

Springdoc OpenAPI UI:

- Main backend: `http://localhost:8080/swagger-ui/index.html`
- Authentication API: `http://localhost:8081/swagger-ui/index.html`

---

## Debugging

### Backend (IntelliJ / VS Code)

Run the Spring Boot application in debug mode:

```
./gradlew bootRun --debug-jvm
```

Then attach your debugger to `localhost:5005`.

### Frontend

Use your browser devtools with `npm run dev`.

---

## Common Troubleshooting

### Stop a service running on a specific port

Find the process ID (PID) and kill it:

```bash
# Find and kill in one command (replace 8080 with the port you need)
kill $(lsof -ti :8080)

# Or step by step:
lsof -ti :8080          # prints the PID
kill <PID>              # graceful stop (SIGTERM)
kill -9 <PID>           # force kill if graceful stop does not work
```

Common ports used by this project:

| Port | Service |
|------|---------|
| 8080 | Spring Boot backend |
| 8081 | Authentication API |
| 5173 | Vite frontend / Nginx (dev-friendly alias) |
| 80   | Nginx frontend |
| 3306 | MySQL |
| 3000 | Grafana |
| 3100 | Loki |
| 3200 | Tempo |
| 4317 | OTel Collector gRPC |
| 4318 | OTel Collector HTTP |
| 9090 | Prometheus |

- **Port 8080 or 3306 already in use**: stop conflicting services or change ports in `application.yml` / `docker-compose.yml`.
- **Liquibase errors on startup**: check database connection and ensure schema is clean; review `db.changelog-master.yaml`.
- **Document storage issues**: verify `storage.base-path` and ensure the folder is writable.
- **No metrics in Prometheus**: ensure `OTLP_METRICS_ENABLED=true` and that `car-reselling-api` is on the `car-reselling-net` network so it can reach `otel-collector`.
- **No traces in Tempo**: ensure `OBSERVABILITY_TRACING_ENABLED=true` and check OTel Collector logs (`docker logs otel-collector`).
- **No logs in Loki**: check that `LOKI_URL` points to the correct container name and that the shared network is in place.
- **Grafana shows "Data source not found"**: the datasources are provisioned automatically; restart Grafana if the volume was lost (`docker compose restart grafana`).

---

## Commands Summary

```bash
# ── Environment variables ────────────────────────────────────────────────────

# Load .env into the current terminal only (lost when terminal closes)
source ./load-env.sh

# Load into the current terminal AND persist for all future terminals
source ./load-env.sh --global

# Remove from RC file (stops loading in new terminals)
./load-env.sh --remove-global

# Unset variables from the current session
source ./load-env.sh --unset-session

# Check status (is it in the RC file? set in current session?)
./load-env.sh --status

# ── Development stack ────────────────────────────────────────────────────────

docker compose up --build

# Development stack + observability (create network first)
docker network create car-reselling-net
docker compose -f docker-compose.yml -f docker-compose-observality.yml up -d

# ── Production stack ─────────────────────────────────────────────────────────

docker compose -f docker-compose-prod.yml up -d

# Production stack + observability
docker network create car-reselling-net
docker compose -f docker-compose-prod.yml -f docker-compose-observality.yml up -d

# ── Local development ────────────────────────────────────────────────────────

# Backend
cd backend && ./gradlew bootRun

# Frontend
cd frontend && npm install && npm run dev

# ── Build backend JAR ────────────────────────────────────────────────────────

# Build with the default version from build.gradle
./build-api.sh

# Build and stamp a specific version
./build-api.sh 1.8

# Build a specific version, skipping tests
./build-api.sh --skip-tests 1.8

# Show all options
./build-api.sh --help

# ── Logs & cleanup ───────────────────────────────────────────────────────────

# View logs for a service
docker logs -f car-reselling-api
docker logs -f otel-collector

# Stop everything and remove containers
docker compose down
docker compose -f docker-compose-prod.yml down

# ── Kubernetes ────────────────────────────────────────────────────────────────

# Find Docker bridge host gateway IP (used in DB/Loki/OTel URLs across clusters)
docker network inspect kind --format='{{range .IPAM.Config}}{{.Gateway}}{{end}}'

# Start external MySQL (required by both APIs)
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=car_reselling \
  -e MYSQL_USER=car -e MYSQL_PASSWORD=car \
  -p 3306:3306 mysql:8.0

# Create all three clusters
kind create cluster --config kubernetes/observability-cluster.yml     --name observability
kind create cluster --config kubernetes/car-reselling-api-cluster.yml --name car-reselling
kind create cluster --config kubernetes/authentication-api-cluster.yml --name authentication

# List clusters and contexts
kind get clusters
kubectl config get-contexts

# Check / switch active context (ALWAYS verify before apply/delete)
kubectl config current-context
kubectl config use-context kind-observability
kubectl config use-context kind-car-reselling
kubectl config use-context kind-authentication

# ── Deploy observability stack ────────────────────────────────────
kubectl config use-context kind-observability
kubectl apply -f kubernetes/observability-deployment.yml
kubectl get pods -n observability -w

# ── Deploy car-reselling-api ──────────────────────────────────────
kubectl config use-context kind-car-reselling
kind load docker-image viniciussf/car-reselling-api:latest --name car-reselling
kubectl apply -f kubernetes/car-reselling-api-deployment.yml
kubectl get pods -n car-reselling -w

# ── Deploy authentication-api ─────────────────────────────────────
kubectl config use-context kind-authentication
kind load docker-image viniciussf/authentication-api:latest --name authentication
kubectl apply -f kubernetes/authentication-api-deployment.yml
kubectl get pods -n authentication -w

# ── Watch all pods across all namespaces ──────────────────────────
kubectl get pods -A -o wide

# ── Logs ──────────────────────────────────────────────────────────
kubectl logs -f -l app=grafana            -n observability  --tail=100
kubectl logs -f -l app=car-reselling-api  -n car-reselling  --tail=100
kubectl logs -f -l app=authentication-api -n authentication --tail=100
# Logs from a previously crashed pod
kubectl logs -l app=car-reselling-api -n car-reselling --previous

# ── Check env vars injected into a running pod ────────────────────
kubectl exec -n car-reselling  deployment/car-reselling-api  -- env | grep -E "DB_URL|LOKI|OTLP"
kubectl exec -n authentication deployment/authentication-api -- env | grep -E "DB_URL|JWT"

# ── Restart after ConfigMap / Secret changes ──────────────────────
kubectl rollout restart deployment/car-reselling-api  -n car-reselling
kubectl rollout restart deployment/authentication-api -n authentication
kubectl rollout restart deployment/grafana            -n observability

# ── HPA status ────────────────────────────────────────────────────
kubectl get hpa -n car-reselling
kubectl describe hpa car-reselling-api-hpa -n car-reselling

# ── Rollback to previous image ────────────────────────────────────
kubectl rollout undo deployment/car-reselling-api -n car-reselling

# ── Delete a stuck Service and recreate it ────────────────────────
kubectl delete svc car-reselling-api -n car-reselling
kubectl apply  -f kubernetes/car-reselling-api-deployment.yml

# ── Remove all resources (keeps clusters) ────────────────────────
kubectl delete -f kubernetes/observability-deployment.yml     --context kind-observability
kubectl delete -f kubernetes/car-reselling-api-deployment.yml  --context kind-car-reselling
kubectl delete -f kubernetes/authentication-api-deployment.yml --context kind-authentication

# ── Destroy clusters entirely ─────────────────────────────────────
kind delete cluster --name observability
kind delete cluster --name car-reselling
kind delete cluster --name authentication
```

---

## License

Private project for internal MVP usage.
