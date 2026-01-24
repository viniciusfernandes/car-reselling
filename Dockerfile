FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json ./
RUN npm install
COPY frontend/ .
RUN npm run build

FROM gradle:8.7-jdk21 AS backend-build
WORKDIR /app/backend
COPY backend/ .
COPY --from=frontend-build /app/frontend/dist /app/backend/src/main/resources/static
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
