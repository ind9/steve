#!/usr/bin/env bash

sbt compile test

sbt dist

cd steve-service

rm -rf dist

mkdir dist

tar  --exclude=src --exclude=.git --exclude=dist  -czf dist/steve-service.tgz .
