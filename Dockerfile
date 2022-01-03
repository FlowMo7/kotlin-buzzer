FROM openjdk:8-jre-alpine

COPY ./build/libs/Buzzer.jar /usr/src/buzzer/Buzzer.jar

WORKDIR /usr/src/buzzer

EXPOSE 8080

HEALTHCHECK CMD curl -f http://0.0.0.0:8080/status || exit 1

CMD ["java", "-jar", "Buzzer.jar"]
