FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine

RUN mkdir /application_server

COPY entrypoint.sh /application_server/entrypoint.sh
COPY build/libs/*.jar /application_server/spring-boot-application.jar

ENTRYPOINT ["/application_server/entrypoint.sh"]
