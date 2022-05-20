package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;

/**
 * Subscribe to a topic ID's messages from a mirror node. You will receive
 * all messages for the specified topic or within the defined start and end
 * time.
 *
 * {@link https://docs.hedera.com/guides/docs/sdks/consensus/get-topic-message}
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
