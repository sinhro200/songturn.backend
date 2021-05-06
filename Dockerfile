#FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
#FROM adoptopenjdk/openjdk11:latest

FROM adoptopenjdk/openjdk11:jdk-11.0.8_10-alpine

#RUN apk update && apk add bash
RUN mkdir /application_server

#WORKDIR /application_server

COPY entrypoint.sh /application_server/entrypoint.sh
COPY build/libs/*.jar /application_server/spring-boot-application.jar

RUN chmod 755 /application_server/*.sh
RUN chmod a+x /application_server/entrypoint.sh
RUN chmod a+x /application_server/spring-boot-application.jar

CMD java -XX:+UseContainerSupport -Xmx256m -Xss512k -XX:MetaspaceSize=100m -jar /application_server/spring-boot-application.jar

#Не смог выполнить, вероятная впричина в том, что
#Почему то не видит bash и sh, поэтому не выполняется скрипт ниже
#ENTRYPOINT ["/application_server/entrypoint.sh"]

##!/usr/bin/env bash