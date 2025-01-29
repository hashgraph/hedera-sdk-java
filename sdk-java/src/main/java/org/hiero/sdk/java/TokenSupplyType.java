// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

/**
 * Possible Token Supply Types (IWA Compatibility).
 * <p>
 * Indicates how many tokens can have during its lifetime.
 * <p>
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/basic-types/tokensupplytype">Hedera Documentation</a>
 */
public enum TokenSupplyType {
    /**
     * Indicates that tokens of that type have an upper bound of Long.MAX_VALUE.
     */
    INFINITE(org.hiero.sdk.java.proto.TokenSupplyType.INFINITE),
    /**
     * Indicates that tokens of that type have an upper bound of maxSupply, provided on token creation.
     */
    FINITE(org.hiero.sdk.java.proto.TokenSupplyType.FINITE);

    final org.hiero.sdk.java.proto.TokenSupplyType code;

    /**
     * Constructor.
     *
     * @param code the token supply type
     */
    TokenSupplyType(org.hiero.sdk.java.proto.TokenSupplyType code) {
        this.code = code;
    }

    /**
     * What type are we.
     *
     * @param code the token supply type in question
     * @return the token supply type
     */
    static TokenSupplyType valueOf(org.hiero.sdk.java.proto.TokenSupplyType code) {
        return switch (code) {
            case INFINITE -> INFINITE;
            case FINITE -> FINITE;
            default -> throw new IllegalStateException("(BUG) unhandled TokenSupplyType");
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case INFINITE -> "INFINITE";
            case FINITE -> "FINITE";
        };
    }

    public org.hiero.sdk.java.proto.TokenSupplyType toProtobuf() {
        return this.code;
    }
}
