#!/usr/bin/env bash

./gradlew jar
docker build --pull -t batix/rundeck-ansible:latest -t batix/rundeck-ansible:${TRAVIS_TAG} .
docker login -u="${DOCKER_USERNAME}" -p="${DOCKER_PASSWORD}"
docker push batix/rundeck-ansible
