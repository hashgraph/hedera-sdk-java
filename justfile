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

publish:
    #!/usr/bin/env bash
    git diff-index --quiet HEAD
    if [ $? -ne 0 ]; then
        echo "Aborting: uncommitted changes detected"
        exit 1
    fi
    git clean -ffdx
    ./gradlew publishToSonatype
    perl -p -i -e "s#jdk7#jdk9#g" sdk/build.gradle
    git clean -ffdx
    rm -f sdk/src/main/java/com/hedera/hashgraph/sdk/FutureConverter.java
    FILES="sdk/src/main/java/com/hedera/hashgraph/sdk/*.java executable-processor/src/main/java/com/hedera/hashgraph/sdk/*.java"
    perl -p -i -e "s#java8#java#g" $FILES
    perl -p -i -e "s#org\.threeten\.bp#java.time#g" $FILES
    perl -p -i -e "s#J8Arrays#Arrays#g" $FILES
    perl -p -i -e "s#IntStreams#IntStream#g" $FILES
    perl -p -i -e "s#\bLists\b#List#g" $FILES
    perl -p -i -e "s#StreamSupport\.stream\((.*)\).map#\1.stream().map#g" $FILES
    perl -p -i -e "s#import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;#import net.javacrumbs.futureconverter.guavacommon.GuavaFutureUtils;\nimport net.javacrumbs.futureconverter.java8common.Java8FutureUtils;#g" $FILES
    perl -p -i -e "s#toCompletableFuture\((.*)\).handle#Java8FutureUtils.createCompletableFuture(GuavaFutureUtils.createValueSource(\1)).handle#g" $FILES
    ./gradlew publishToSonatype
    git restore sdk executable-processor


update-proto:
    ./scripts/update_protobufs.py master

update: update-proto build

_hard-clean:
    git clean -ffdx
