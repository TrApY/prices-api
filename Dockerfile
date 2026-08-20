# Builder: compila con el wrapper y trocea el jar en capas cacheables.
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar --no-daemon
# Desacoplado de la versión: bootJar produce un único jar.
RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers --destination extracted \
    && mv extracted/application/*.jar extracted/application/app.jar

# Runtime mínimo, sin JDK y sin root.
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
