#!/usr/bin/env bash
protoc --proto_path=./src/main/resources/ ./src/main/resources/depot.proto --java_out=./src/main/java/