package com.hedera.hashgraph.sdk;

/*
DEFAULT = 0;
TOKEN_FUNGIBLE_COMMON = 1;
TOKEN_NON_FUNGIBLE_UNIQUE = 2;
*/


public enum FeeDataType {
    
    DEFAULT(com.hedera.hashgraph.sdk.proto.SubType.DEFAULT),
    
    TOKEN_FUNGIBLE_COMMON(com.hedera.hashgraph.sdk.proto.SubType.TOKEN_FUNGIBLE_COMMON),

    TOKEN_NON_FUNGIBLE_UNIQUE(com.hedera.hashgraph.sdk.proto.SubType.TOKEN_NON_FUNGIBLE_UNIQUE);

    final com.hedera.hashgraph.sdk.proto.SubType code;

    FeeDataType(com.hedera.hashgraph.sdk.proto.SubType code) {
        this.code = code;
    }

    static FeeDataType valueOf(com.hedera.hashgraph.sdk.proto.SubType code) {
        switch (code) {
            case DEFAULT:
                return DEFAULT;
            case TOKEN_FUNGIBLE_COMMON:
                return TOKEN_FUNGIBLE_COMMON;
            case TOKEN_NON_FUNGIBLE_UNIQUE:
                return TOKEN_NON_FUNGIBLE_UNIQUE;
            default:
                throw new IllegalStateException("(BUG) unhandled SubType (FeeDataType) " + code);
        }
    }

    @Override
    public String toString() {
        switch(this)
        {
            case DEFAULT:
                return "DEFAULT";
            case TOKEN_FUNGIBLE_COMMON:
                return "TOKEN_FUNGIBLE_COMMON";
            case TOKEN_NON_FUNGIBLE_UNIQUE:
                return "TOKEN_NON_FUNGIBLE_UNIQUE";
            default:
                return "<UNRECOGNIZED VALUE>";
        }
    }
}