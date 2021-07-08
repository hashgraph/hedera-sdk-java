package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;

@Beta
public enum TokenSupplyType {
    /*
     * Indicates that tokens of that type have an upper bound of Long.MAX_VALUE.
     */
    INFINITE(com.hedera.hashgraph.proto.TokenSupplyType.INFINITE),
    /*
     * Indicates that tokens of that type have an upper bound of maxSupply, provided on token creation.
     */
    FINITE(com.hedera.hashgraph.proto.TokenSupplyType.FINITE);

    final com.hedera.hashgraph.proto.TokenSupplyType code;

    TokenSupplyType(com.hedera.hashgraph.proto.TokenSupplyType code) {
        this.code = code;
    }

    static TokenSupplyType valueOf(com.hedera.hashgraph.proto.TokenSupplyType code) {
        switch (code) {
            case INFINITE:
                return INFINITE;
            case FINITE:
                return FINITE;
            default:
                throw new IllegalStateException("(BUG) unhandled TokenSupplyType " + code);
        }
    }

    @Override
    public String toString() {
        switch(this)
        {
            case INFINITE:
                return "INFINITE";
            case FINITE:
                return "FINITE";
            default:
                return "<UNRECOGNIZED VALUE>";
        }
    }
}
