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

import javax.annotation.Nullable;

/**
 * Subscribe to a topic ID's messages from a mirror node. You will receive
 * all messages for the specified topic or within the defined start and end
 * time.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/consensus/get-topic-message">Hedera Documentation</a>
 */
public final class SubscriptionHandle {
    @Nullable
    private Runnable onUnsubscribe;

    /**
     * Constructor.
     */
    SubscriptionHandle() {
    }

    /**
     * Assign the callback method.
     *
     * @param onUnsubscribe             the callback method
     */
    void setOnUnsubscribe(Runnable onUnsubscribe) {
        this.onUnsubscribe = onUnsubscribe;
    }

    /**
     * Call the callback.
     */
    public void unsubscribe() {
        if (this.onUnsubscribe != null) {
            this.onUnsubscribe.run();
        }
    }
}
