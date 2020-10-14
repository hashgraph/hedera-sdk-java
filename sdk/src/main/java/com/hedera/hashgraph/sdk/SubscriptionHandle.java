package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;

public final class SubscriptionHandle {
    @Nullable
    private Runnable onUnsubscribe;

    SubscriptionHandle() {
    }

    void setOnUnsubscribe(Runnable onUnsubscribe) {
        this.onUnsubscribe = onUnsubscribe;
    }

    public void unsubscribe() {
        if (this.onUnsubscribe != null) {
            this.onUnsubscribe.run();
        }
    }
}
