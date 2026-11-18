# ---- Build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system asgasd && useradd --system --gid asgasd --home /app asgasd \
    && mkdir -p /app/uploads && chown -R asgasd:asgasd /app

COPY --from=build --chown=asgasd:asgasd /build/target/asgasd-digital.jar ./app.jar

ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    UPLOAD_DIR=/app/uploads

USER asgasd
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
