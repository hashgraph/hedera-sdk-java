package com.hedera.hashgraph.sdk;

import java.time.Duration;

public final class HederaConstants {
    private HederaConstants() {}

    // Required fixed default autorenew duration for entities. (roughly 1/4 year)
    public static final Duration DEFAULT_AUTORENEW_DURATION = Duration.ofMinutes(131_500);
}
