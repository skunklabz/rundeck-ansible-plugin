#!/usr/bin/env bash

if [ -d /data/libext ]; then
  echo adding custom plugins...
  cp -r /data/libext/* ${RDECK_BASE}/libext
fi

sed -i -e "s/_ADMINPW_/${RDECK_ADMIN_PASS}/" ${RDECK_BASE}/server/config/realm.properties

java -Xmx1g "-Dserver.address=${RDECK_HOST}" "-Dserver.port=${RDECK_PORT}" -jar "${RDECK_BASE}/${RDECK_WAR}"
