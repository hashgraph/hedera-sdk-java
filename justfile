export PATH := "./node_modules/.bin:" + env_var('PATH')

default:
    just --choose

docs:
    ./gradlew javadoc

build:
    ./gradlew sdk:assemble

lint:
    # We should figure something out here.

format:
    # We should figure something out here.

clean:
    ./gradlew clean

test-unit TEST="*.*":
    ./gradlew test --tests "{{TEST}}"

test-integration-node TEST="*.*":
    #!/usr/bin/env bash
    ./gradlew -POPERATOR_KEY=$OPERATOR_KEY -POPERATOR_ID=$OPERATOR_ID -PCONFIG_FILE=$CONFIG_FILE integrationTest --tests "{{TEST}}"

update-snapshots TEST=".*":
    rm sdk/src/test/java/com/hedera/hashgraph/sdk/{{TEST}}.snap
    ./gradlew test --tests "{{TEST}}.*"

publish: _hard-clean build
    ./gradlew sdk:uploadArchieve

update-proto:
    ./scripts/update_protobufs.py master

update: update-proto build

_hard-clean:
    git clean -ffdx
