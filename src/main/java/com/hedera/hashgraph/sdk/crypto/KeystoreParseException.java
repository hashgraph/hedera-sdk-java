package com.hedera.hashgraph.sdk.crypto;

import com.google.gson.JsonSyntaxException;

public final class KeystoreParseException extends RuntimeException {
    KeystoreParseException(String message) { super(message); }

    KeystoreParseException(JsonSyntaxException e) {
        super(e);
    }
}
