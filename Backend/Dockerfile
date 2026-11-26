# Etap 1: Budowanie aplikacji
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
# Pobieranie zależności (cache)
RUN ./gradlew dependencies --no-daemon
# Kopiowanie kodu i budowanie (z pominięciem testów)
COPY src src
RUN ./gradlew build -x test --no-daemon

# Etap 2: Uruchomienie aplikacji
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Tworzenie bezpiecznego użytkownika
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
# Kopiowanie zbudowanego pliku JAR
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]