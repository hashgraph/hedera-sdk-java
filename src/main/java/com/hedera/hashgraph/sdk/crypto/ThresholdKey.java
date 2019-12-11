package com.hedera.hashgraph.sdk.crypto;

import java.util.Collection;

import javax.annotation.Nonnegative;

public class ThresholdKey extends PublicKey {
    private final com.hedera.hashgraph.proto.ThresholdKey.Builder inner =
        com.hedera.hashgraph.proto.ThresholdKey.newBuilder();

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
    public ThresholdKey add(PublicKey key) {
        inner.getKeysBuilder().addKeys(key.toKeyProto());
        return this;
    }

    public ThresholdKey addAll(Collection<? extends PublicKey> keys) {
        for (final PublicKey key : keys) {
            add(key);
        }

        return this;
    }

    public ThresholdKey addAll(PublicKey... keys) {
        for (final PublicKey key : keys) {
            add(key);
        }

        return this;
    }

    /**
     * @throws IllegalStateException if fewer keys have been added than the threshold set in {@link #ThresholdKey(int)}
     */
    @Override
    public com.hedera.hashgraph.proto.Key toKeyProto() {
        if (inner.getKeysBuilder().getKeysCount() < inner.getThreshold()) {
            throw new IllegalStateException("ThresholdKey must have at least as many keys as the set threshold");
        }

        final com.hedera.hashgraph.proto.Key.Builder key = com.hedera.hashgraph.proto.Key.newBuilder();
        key.setThresholdKey(inner);
        return key.build();
    }
}
