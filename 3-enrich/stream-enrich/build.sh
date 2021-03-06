#!/bin/sh

set -e

ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VERSION=`cat ${ROOT}/version.sbt | awk '{print $5}' | sed 's/"//g'`
NAME="snowplow-stream-enrich"
REPO="ktimothy/${NAME}"
IMAGE="${REPO}:v${VERSION}"

cp -r ../config ./

docker build --no-cache --force-rm -t ${REPO} .
rm -rf ./config
docker tag ${REPO} ${IMAGE}
docker push ${REPO}:latest
docker push ${IMAGE}
