FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre

RUN useradd -ms /bin/bash appuser
WORKDIR /app

COPY --from=builder /build/target/tutorial-java-backend-0.0.1-SNAPSHOT.jar app.jar

RUN chown -R appuser /app
USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]