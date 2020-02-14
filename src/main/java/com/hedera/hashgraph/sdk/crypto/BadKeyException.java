package com.hedera.hashgraph.sdk.crypto;

import com.google.gson.JsonSyntaxException;
import com.hedera.hashgraph.sdk.Internal;

public final class BadKeyException extends IllegalArgumentException {
    @Internal
    public BadKeyException(String message) { super(message); }

    BadKeyException(JsonSyntaxException e) {
        super(e);
    }

    @Internal
    public BadKeyException(Exception e) {
        super(e);
    }
}
