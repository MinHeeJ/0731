FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17.0.10_7-jre-alpine
WORKDIR /app
COPY --from=build /app/target/common-foundation-0.1.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
