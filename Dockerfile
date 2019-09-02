FROM java:openjdk-8-jdk-alpine
MAINTAINER Marcelo Pereira <marcelovcpereira@gmail.com>

RUN mkdir /scripts

COPY ./target/mvcp-adobe-reverse-proxy-1.0-SNAPSHOT.jar /scripts/marcelo-adobe-reverse-proxy.jar


COPY ./src/main/resources/devops/marcelo-adobe-test/docker/docker-entrypoint.sh /scripts/docker-entrypoint.sh
RUN ["chmod", "+x", "/scripts/docker-entrypoint.sh"]

EXPOSE 9090

ENTRYPOINT ["/scripts/docker-entrypoint.sh"]
