package com.hedera.hashgraph.sdk;

/**
 * Possible Token Supply Types (IWA Compatibility).
 *
 * Indicates how many tokens can have during its lifetime.
 *
 * {@link https://docs.hedera.com/guides/docs/hedera-api/basic-types/tokensupplytype}
 */
public enum TokenSupplyType {
    /**
     * Indicates that tokens of that type have an upper bound of Long.MAX_VALUE.
     */
    INFINITE(com.hedera.hashgraph.sdk.proto.TokenSupplyType.INFINITE),
    /**
     * Indicates that tokens of that type have an upper bound of maxSupply, provided on token creation.
     */
    FINITE(com.hedera.hashgraph.sdk.proto.TokenSupplyType.FINITE);

    final com.hedera.hashgraph.sdk.proto.TokenSupplyType code;

    /**
     * Constructor.
     *
     * @param code                      the token supply type
     */
    TokenSupplyType(com.hedera.hashgraph.sdk.proto.TokenSupplyType code) {
        this.code = code;
    }

    /**
     * What type are we.
     *
     * @param code                      the token supply type in question
     * @return                          the token supply type
     */
    static TokenSupplyType valueOf(com.hedera.hashgraph.sdk.proto.TokenSupplyType code) {
        switch (code) {
            case INFINITE:
                return INFINITE;
            case FINITE:
                return FINITE;
            default:
                throw new IllegalStateException("(BUG) unhandled TokenSupplyType");
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case INFINITE:
                return "INFINITE";
            case FINITE:
                return "FINITE";
            default:
                return "<UNRECOGNIZED VALUE>";
        }
    }
}
