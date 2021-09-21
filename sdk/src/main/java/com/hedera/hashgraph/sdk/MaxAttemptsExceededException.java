package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;

public class MaxAttemptsExceededException extends Exception {
    MaxAttemptsExceededException(@Nullable Throwable e) {
        super("exceeded maximum attempts for request with last exception being", e);
    }
}
