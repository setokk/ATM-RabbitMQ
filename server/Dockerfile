FROM openjdk:21-jdk-bullseye

WORKDIR /app

RUN apt-get update && apt-get install -y maven

COPY pom.xml /app
COPY src/main/java/atm /app/src

RUN mvn package

# Wait for db to start
COPY wait-for-it.sh ./wait-for-it.sh
RUN chmod +x ./wait-for-it.sh
ENTRYPOINT ["./wait-for-it.sh", "db:5432", "--", "./wait-for-it.sh", "rabbitmq:5672", "--", "java", "-jar", "target/ATM-RabbitMQ-Server-1.0.jar"]