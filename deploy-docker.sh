#!/usr/bin/env bash

./gradlew jar
# strip leading 'v' from the tag
docker build --pull -t batix/rundeck-ansible:latest -t batix/rundeck-ansible:${TRAVIS_TAG#v} .
docker login -u="${DOCKER_USERNAME}" -p="${DOCKER_PASSWORD}"
docker push batix/rundeck-ansible
