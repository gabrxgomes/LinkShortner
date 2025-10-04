# Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/link-shortener-1.0.0.jar app.jar
EXPOSE $PORT
CMD java -Dserver.port=$PORT -jar app.jar
