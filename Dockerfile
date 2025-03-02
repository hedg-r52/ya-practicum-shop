FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle clean bootJar

FROM openjdk:21-jdk

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

RUN chmod +x /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]