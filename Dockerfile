FROM openjdk:11-jdk-slim

EXPOSE 4000:4000

RUN mkdir /yaam

### used build/docker folder as working directory for docker plugin
COPY ./build/install/yaam/ /yaam
COPY ./src/main/resources/application-dev.conf /yaam/bin/application.conf

WORKDIR /yaam/bin

ENTRYPOINT ["./yaam", "-config=./application.conf"]


