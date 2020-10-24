FROM openjdk:14.0.2-jdk AS builder
WORKDIR /mm
COPY . .
RUN ./gradlew assemble

FROM openjdk:14.0.2-jdk
WORKDIR /
RUN mkdir money-manager
COPY --from=builder /mm/build/libs/money-manager-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]

