package com.hedera.hashgraph.tck.methods;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.hedera.hashgraph.sdk.Status;

/**
 * Custom JSON-RPC error definitions
 */
public class JSONRPC2Error {
    private JSONRPC2Error() {
        // static utility class
    }

    public static final int HEDERA_STATUS_CODE = -32001;
    public static final com.thetransactioncompany.jsonrpc2.JSONRPC2Error HEDERA_ERROR =
            new com.thetransactioncompany.jsonrpc2.JSONRPC2Error(HEDERA_STATUS_CODE, "Hedera error");

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public record ErrorData(Status status, String message) {}
}
