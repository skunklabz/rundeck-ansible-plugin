#!/usr/bin/env bash

./gradlew jar
docker build --pull -t batix/rundeck-ansible .
docker login -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"
docker push batix/rundeck-ansible
