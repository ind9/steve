#!/bin/bash

set -x

sbt +publishSigned         #This will sign and upload your artifact to Sonatype’s staging repository.
sbt sonatypeRelease      #This promotes the release to be ready for syncing to Maven Central.

echo "Released"
