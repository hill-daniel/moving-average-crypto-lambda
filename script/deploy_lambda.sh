#!/usr/bin/env bash

aws lambda update-function-code \
--region eu-central-1 \
--function-name floating-average-crypto \
--zip-file fileb://../target/floating-average-crypto.jar \
--publish