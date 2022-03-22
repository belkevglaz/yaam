#!/bin/bash

./gradlew :clean :installDist && \
docker build -t yaam:$1 -f ./src/main/docker/Dockerfile .