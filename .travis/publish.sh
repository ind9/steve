#!/bin/bash

set -x -e

sbt 'project steveCore' +publishSigned
sbt 'project steveScalaClient' +publishSigned
sbt sonatypeReleaseAll

echo "Released"
