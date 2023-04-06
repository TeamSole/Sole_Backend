FROM eclipse-temurin:11
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=local","/app.jar"]