FROM openjdk:8-jre-alpine

COPY ./build/libs/Buzzer.jar /usr/src/buzzer/Buzzer.jar

WORKDIR /usr/src/buzzer

EXPOSE 8080

CMD ["java", "-jar", "Buzzer.jar"]
