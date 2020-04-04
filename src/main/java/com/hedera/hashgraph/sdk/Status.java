package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;

public enum Status {
    /** The transaction passed the pre-check validation. */
    Ok(ResponseCodeEnum.OK);

    private final ResponseCodeEnum code;

    Status(ResponseCodeEnum code) {
        this.code = code;
    }

    static Status valueOf(ResponseCodeEnum code) {
        switch (code) {
            case OK:
                return Ok;

            case UNRECOGNIZED:
                // NOTE: Protobuf deserialization will not give us the code on the wire
                throw new IllegalArgumentException(
                        "network return unrecognized response code; update your SDK or open an issue");

            default:
                throw new IllegalArgumentException(
                        "response code "
                                + code.name()
                                + " is unhandled by the SDK; update your SDK or open an issue");
        }
    }

    @Override
    public String toString() {
        return code.name();
    }
}
