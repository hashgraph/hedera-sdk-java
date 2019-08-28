package com.hedera.hashgraph.sdk.crypto;

import java.util.Collection;

import javax.annotation.Nonnegative;

public class ThresholdKey implements Key {
    private final com.hederahashgraph.api.proto.java.ThresholdKey.Builder inner =
        com.hederahashgraph.api.proto.java.ThresholdKey.newBuilder();

    /**
     * Create a new threshold-key with the given threshold.
     **/
    public ThresholdKey(@Nonnegative int threshold) {
        inner.setThreshold(threshold);
    }

    /**
     * Add another key to the list of this ThresholdKey.
     *
     * @return {@code this} for fluent API usage.
     */
    public ThresholdKey add(Key key) {
        inner.getKeysBuilder().addKeys(key.toKeyProto());
        return this;
    }

    public ThresholdKey addAll(Collection<? extends Key> keys) {
        for (final var key : keys) {
            add(key);
        }

        return this;
    }

    public ThresholdKey addAll(Key... keys) {
        for (final var key : keys) {
            add(key);
        }

        return this;
    }

    /**
     * @throws IllegalStateException if fewer keys have been added than the threshold set in {@link #ThresholdKey(int)}
     */
    @Override
    public com.hederahashgraph.api.proto.java.Key toKeyProto() {
        if (inner.getKeysBuilder().getKeysCount() < inner.getThreshold()) {
            throw new IllegalStateException("ThresholdKey must have at least as many keys as the set threshold");
        }

        final var key = com.hederahashgraph.api.proto.java.Key.newBuilder();
        key.setThresholdKey(inner);
        return key.build();
    }
}
