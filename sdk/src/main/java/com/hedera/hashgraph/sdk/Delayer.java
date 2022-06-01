/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
/**
 * Utility class used internally by the sdk.
 */
final class Delayer {
    private static final Logger logger = LoggerFactory.getLogger(Delayer.class);

    private static final Duration MIN_DELAY = Duration.ofMillis(500);

    /**
     * Constructor.
     */
    private Delayer() {
    }

    /**
     * Set the delay backoff attempts.
     *
     * @param attempt                   the attempts
     * @param executor                  the executor
     * @return                          the updated future
     */
    static CompletableFuture<Void> delayBackOff(int attempt, Executor executor) {
        var interval = MIN_DELAY.multipliedBy(ThreadLocalRandom.current().nextLong(1L << attempt));

        return delayFor(interval.toMillis(), executor);
    }

    /**
     * Set the delay backoff milliseconds.
     *
     * @param milliseconds              the milliseconds
     * @param executor                  the executor
     * @return                          the updated future
     */
    static CompletableFuture<Void> delayFor(long milliseconds, Executor executor) {
        logger.trace("waiting for {} seconds before trying again", (double) milliseconds / 1000.0);

        return CompletableFuture.runAsync(
            () -> {
            },
            CompletableFuture.delayedExecutor(milliseconds, TimeUnit.MILLISECONDS, executor));
    }
}
