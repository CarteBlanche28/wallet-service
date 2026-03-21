FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
ARG JAR_FILE=target/wallet-service-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/wallet-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]