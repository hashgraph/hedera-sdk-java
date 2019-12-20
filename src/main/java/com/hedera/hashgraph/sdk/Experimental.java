package com.hedera.hashgraph.sdk;

import java.util.Optional;

public final class Experimental {
    public static final String PROPERTY = "com.hedera.hashgraph.sdk.experimental";

    private Experimental() {
    }

    // Check for experimental property and throw if not set
    public static void requireFor(String className) {
        if (!Optional.ofNullable(System.getProperty(PROPERTY)).orElse("").equals("true")) {
            throw new IllegalStateException("" + className + " is experimental and may not be "
                + "generally available; set the system property "
                + "\"com.hedera.hashgraph.sdk.experimental\" to \"true\" to proceed");
        }
    }
}
