FROM eclipse-temurin:21-jre
COPY target/exam-platform-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 7860
ENTRYPOINT ["java", "-jar", "/app.jar", "--server.port=7860"]


