FROM openjdk:21-jdk-bullseye

WORKDIR /app

RUN apt-get update && apt-get install -y maven

COPY pom.xml /app
COPY src/main/java/client /app/src

RUN mvn package

# Wait for db to start
ENTRYPOINT ["java", "-jar", "target/ATM-RabbitMQ-Client-1.0.jar"]