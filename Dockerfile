FROM alpine
MAINTAINER David Kirstein <dak@batix.com>

ENV ANSIBLE_HOST_KEY_CHECKING=false
ENV RDECK_BASE=/opt/rundeck
ENV MANPATH=${MANPATH}:${RDECK_BASE}/docs/man
ENV PATH=${PATH}:${RDECK_BASE}/tools/bin
ENV PROJECT_BASE=${RDECK_BASE}/projects/Test-Project
ENV RDECK_ADMIN_PASS=rdtest2018
ENV RDECK_HOST=localhost
ENV RDECK_PORT=4440

# install Ansible and Java, Rundeck via launcher, create directories
RUN apk --no-cache add sudo bash ca-certificates curl openssl python2 ansible openjdk8-jre && \
  mkdir -p /etc/ansible \
  ${PROJECT_BASE}/acls \
  ${PROJECT_BASE}/etc \
  ${RDECK_BASE}/libext

# Add default admin account, default project and startup script
COPY docker/realm.properties ${RDECK_BASE}/server/config/
COPY docker/project.properties ${PROJECT_BASE}/etc/
COPY docker/run.sh /
RUN chmod +x /run.sh
CMD /run.sh

# Add Rundeck and locally built plugin
# These are done late to preserve earlier layers
# Rundeck version: http://rundeck.org/downloads.html
ENV RDECK_WAR=rundeck-3.0.1-20180803.war
RUN curl -SLo "${RDECK_BASE}/${RDECK_WAR}" https://dl.bintray.com/rundeck/rundeck-maven/${RDECK_WAR}
COPY build/libs/ansible-plugin-*.jar ${RDECK_BASE}/libext/
