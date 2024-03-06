package com.hedera.hashgraph.tck.methods;

import lombok.Value;

/**
 * Custom JSON-RPC error definitions
 */
public class JSONRPC2Error {
    public static final int HEDERA_STATUS_CODE = -32001;
    public static final com.thetransactioncompany.jsonrpc2.JSONRPC2Error HEDERA_ERROR =
            new com.thetransactioncompany.jsonrpc2.JSONRPC2Error(HEDERA_STATUS_CODE, "Hedera error");

    @Value
    public static class ErrorData {
        String status;
        String message;
    }
}
