#!/bin/bash
set -e

BASE_VERSION="1.0"

if [ -n "$TAG_NAME" ]; then
    VERSION=$(echo $TAG_NAME | sed 's/^v//')
else
    GIT_HASH=$(git rev-parse --short HEAD)
    VERSION="${BASE_VERSION}.${BUILD_NUMBER}-${GIT_HASH}-SNAPSHOT"
fi

echo "Setting version to $VERSION"

mvn versions:set -DnewVersion=$VERSION -DgenerateBackupPoms=false

