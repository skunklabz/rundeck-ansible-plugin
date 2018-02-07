FROM alpine
MAINTAINER David Kirstein <dak@batix.com>

# combining stuff from:
# https://github.com/colebrumley/docker-rundeck
# https://github.com/William-Yeh/docker-ansible

ENV ANSIBLE_HOST_KEY_CHECKING=false
ENV RDECK_BASE=/opt/rundeck
ENV MANPATH=${MANPATH}:${RDECK_BASE}/docs/man
ENV PATH=${PATH}:${RDECK_BASE}/tools/bin
ENV PROJECT_BASE=${RDECK_BASE}/projects/Test-Project
ENV RDECK_ADMIN_PASS=rdtest2017
ENV RDECK_HOST=localhost
ENV RDECK_JAR=${RDECK_BASE}/rundeck-launcher.jar

# install Ansible and Java, Rundeck via launcher, create directories
# check newest version: https://pypi.python.org/pypi/ansible http://rundeck.org/downloads.html
RUN apk --no-cache add sudo bash ca-certificates curl openjdk8-jre openssl py-pip python sudo && \
  apk --no-cache add --virtual build-deps build-base libffi-dev openssl-dev python-dev && \
  pip --no-cache-dir install --upgrade cffi pip && \
  pip --no-cache-dir install ansible==2.3.2.0 && \
  apk del build-deps && \
  mkdir -p /etc/ansible \
  ${PROJECT_BASE}/acls \
  ${PROJECT_BASE}/etc \
  ${RDECK_BASE}/libext && \
  curl -SLo ${RDECK_JAR} https://dl.bintray.com/rundeck/rundeck-maven/rundeck-launcher-2.10.6.jar

COPY docker/realm.properties ${RDECK_BASE}/server/config/
COPY docker/run.sh /
# create project
COPY docker/project.properties ${PROJECT_BASE}/etc/
# install locally built plugin
COPY build/libs/ansible-plugin-*.jar ${RDECK_BASE}/libext/

RUN chmod +x /run.sh

CMD /run.sh
