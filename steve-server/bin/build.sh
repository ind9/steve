#!/usr/bin/env bash

rm -rf steve-server/target/universal

sbt compile test

sbt dist

cd steve-server

rm -rf dist

mkdir dist

tar  --exclude=src --exclude=.git --exclude=dist  -czf dist/steve-service.tgz .
