#!/usr/bin/env bash


PORT0=${PORT0:-9092}
PORT1=${PORT1:-9093}

sudo apt-get install -y unzip

unzip -o target/universal/*.zip && mv steve-server-1.0.0-* steve-server-1.0-SNAPSHOT

./steve-server-1.0-SNAPSHOT/bin/steve-server "-Ddw.server.applicationConnectors[0].port=${PORT0}" "-Ddw.server.adminConnectors[0].port=${PORT1}"  server conf/steve.yml
