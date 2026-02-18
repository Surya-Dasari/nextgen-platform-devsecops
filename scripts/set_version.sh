#!/bin/bash

set -e

if [ -n "$TAG_NAME" ]; then
    VERSION=$(echo $TAG_NAME | sed 's/^v//')
else
    VERSION="1.0.${BUILD_NUMBER}-SNAPSHOT"
fi

echo "Setting version to $VERSION"

mvn versions:set -DnewVersion=$VERSION
mvn versions:commit
