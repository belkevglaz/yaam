FROM openjdk:11-jdk-slim

EXPOSE 4000:4000

RUN mkdir /yaam

COPY build/install/yaam/ /yaam
COPY src/main/resources/application-dev.conf /yaam/bin

WORKDIR /yaam/bin

ENTRYPOINT ["./yaam", "-config=./application-dev.conf"]


