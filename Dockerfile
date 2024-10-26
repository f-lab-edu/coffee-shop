FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Xms500m", "-Xmx1g", "-Dspring.config.location=classpath:/application-docker.properties", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
