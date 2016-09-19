#!/bin/sh

set -e

docker build --no-cache --force-rm -t ktimothy/snowplow-stream-collector .
docker tag ktimothy/snowplow-stream-collector ktimothy/snowplow-stream-collector:v0.8.0-kt
docker push ktimothy/snowplow-stream-collector
