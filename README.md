# Car Reselling MVP

End-to-end MVP for a used car reseller in Brazil. The application includes a Java 21 + Spring Boot 3.x backend and a React 18 + TailwindCSS frontend, with MySQL as the database and Liquibase for schema migrations.

## Features

- Vehicle registration, listing, and detail view
- Service management per vehicle with totals
- Document upload, download, and deletion (local storage)
- Distribution to partner dealerships
- Distributed vehicles report with totals

## Tech Stack

**Backend**
- Java 21, Spring Boot 3.x
- Gradle 8.7
- MySQL 8.x
- Liquibase
- Springdoc OpenAPI

**Frontend**
- React 18 + TypeScript
- TailwindCSS
- Vite
- Axios

## Project Structure

```
.
├── backend
├── frontend
├── context
├── docker-compose.yml
└── Dockerfile
```

## Prerequisites

- Java 21
- Gradle 8.7 (or use Gradle wrapper if added later)
- Node.js 20+
- npm 9+
- Docker 27.5.1 / Docker Compose 2.29.2 (for containerized setup)
- MySQL 8.x (if running locally without Docker)

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

#### HTTPS (Let’s Encrypt)

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

Docker note: mount the Let’s Encrypt folder into the container and point
`SERVER_SSL_KEY_STORE` to the mounted file path.

Example docker compose override:

```
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

### Frontend

Vite dev server runs on `http://localhost:5173` and proxies `/api` to `http://localhost:8080`.

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

Show all tables:

```
docker compose exec mysql mysql -u car -p car_reselling
SHOW TABLES;
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

## Build & Run

### Option A — Docker (recommended)

Build and run everything:

```
docker compose up --build
```

Then open:

- App: `http://localhost:8080`
- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Stop:

```
docker compose down
```

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

## API Documentation

Springdoc OpenAPI UI:

```
http://localhost:8080/swagger-ui.html
```

## Debugging

### Backend (IntelliJ / VS Code)

Run the Spring Boot application in debug mode:

```
./gradlew bootRun --debug-jvm
```

Then attach your debugger to `localhost:5005`.

### Frontend

Use your browser devtools with `npm run dev`.

## Common Troubleshooting

- **Port 8080 or 3306 already in use**: stop conflicting services or change ports in `application.yml` / `docker-compose.yml`.
- **Liquibase errors on startup**: check database connection and ensure schema is clean; review `db.changelog-master.yaml`.
- **Document storage issues**: verify `storage.base-path` and ensure the folder is writable.

## Commands Summary

```
# Docker (full stack)
docker compose up --build

# Backend
cd backend
./gradlew bootRun

# Frontend
cd frontend
npm install
npm run dev
```

## License

Private project for internal MVP usage.
