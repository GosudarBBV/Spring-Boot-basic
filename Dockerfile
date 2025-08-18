# Builder stage
FROM openjdk:17-jdk-alpine as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY target/master.on.time-0.0.1-SNAPSHOT.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Final stage
FROM openjdk:17-jdk-alpine
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
EXPOSE 8080