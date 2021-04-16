# vim: set syntax=dockerfile:

FROM maven:3.6.3-openjdk-8 as build-stage-1

COPY pom.xml ./
RUN mvn -B dependency:resolve dependency:resolve-plugins

COPY src ./src/
RUN mvn -B install
