# vim: set syntax=dockerfile:

#FROM maven:3.6.3-openjdk-8 as build-stage-1
# see https://github.com/OpertusMundi/docker-library/blob/master/spring-boot-builder/Dockerfile
FROM opertusmundi/spring-boot-builder:1-2.3.4

COPY pom.xml ./
RUN mvn -B dependency:resolve-plugins
RUN mvn -B dependency:resolve -DincludeScope=runtime

COPY src ./src/
RUN mvn -B install
