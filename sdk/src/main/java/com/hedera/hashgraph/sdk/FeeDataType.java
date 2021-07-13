package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.SubType;


public enum FeeDataType {

    DEFAULT(SubType.DEFAULT),

    TOKEN_FUNGIBLE_COMMON(SubType.TOKEN_FUNGIBLE_COMMON),

    TOKEN_NON_FUNGIBLE_UNIQUE(SubType.TOKEN_NON_FUNGIBLE_UNIQUE);

    final SubType code;

    FeeDataType(SubType code) {
        this.code = code;
    }

    static FeeDataType valueOf(SubType code) {
        switch (code) {
            case DEFAULT:
                return DEFAULT;
            case TOKEN_FUNGIBLE_COMMON:
                return TOKEN_FUNGIBLE_COMMON;
            case TOKEN_NON_FUNGIBLE_UNIQUE:
                return TOKEN_NON_FUNGIBLE_UNIQUE;
            default:
                throw new IllegalStateException("(BUG) unhandled SubType (FeeDataType)");
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
