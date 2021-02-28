#!/usr/bin/env sh

java -XX:+UseContainerSupport -Xmx256m -Xss512k -XX:MetaspaceSize=100m -jar /application_server/spring-boot-application.jar