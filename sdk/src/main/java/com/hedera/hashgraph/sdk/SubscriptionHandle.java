package com.hedera.hashgraph.sdk;

public final class SubscriptionHandle {
    private final Runnable onUnsubscribe;

    SubscriptionHandle(Runnable onUnsubscribe) {
        this.onUnsubscribe = onUnsubscribe;
    }

    public void unsubscribe() {
        this.onUnsubscribe.run();
    }
}
