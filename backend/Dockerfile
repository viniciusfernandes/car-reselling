# -------------------------
# Stage 1: Build backend jar
# -------------------------
FROM gradle:8.7-jdk21 AS backend-build

WORKDIR /app/backend

# Copy only backend project
COPY backend/ .

# Build the Spring Boot executable jar
RUN gradle bootJar --no-daemon


# -------------------------
# Stage 2: Runtime image
# -------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]