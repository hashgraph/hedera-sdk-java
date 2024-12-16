// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.hiero.sdk.proto.SubType;

/**
 * Enum for the fee data types.
 */
public enum FeeDataType {
    /**
     * The resource prices have no special scope
     */
    DEFAULT(SubType.DEFAULT),

    /**
     * The resource prices are scoped to an operation on a fungible common token
     */
    TOKEN_FUNGIBLE_COMMON(SubType.TOKEN_FUNGIBLE_COMMON),

    /**
     * The resource prices are scoped to an operation on a non-fungible unique token
     */
    TOKEN_NON_FUNGIBLE_UNIQUE(SubType.TOKEN_NON_FUNGIBLE_UNIQUE),

    /**
     * The resource prices are scoped to an operation on a fungible common
     * token with a custom fee schedule
     */
    TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES(SubType.TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES),

    /**
     * The resource prices are scoped to an operation on a non-fungible unique
     * token with a custom fee schedule
     */
    TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES(SubType.TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES),

    /**
     * The resource prices are scoped to a ScheduleCreate containing a ContractCall.
     */
    SCHEDULE_CREATE_CONTRACT_CALL(SubType.SCHEDULE_CREATE_CONTRACT_CALL);

    final SubType code;

    FeeDataType(SubType code) {
        this.code = code;
    }

    static FeeDataType valueOf(SubType code) {
        return switch (code) {
            case DEFAULT -> DEFAULT;
            case TOKEN_FUNGIBLE_COMMON -> TOKEN_FUNGIBLE_COMMON;
            case TOKEN_NON_FUNGIBLE_UNIQUE -> TOKEN_NON_FUNGIBLE_UNIQUE;
            case TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES -> TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES;
            case TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES -> TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES;
            case SCHEDULE_CREATE_CONTRACT_CALL -> SCHEDULE_CREATE_CONTRACT_CALL;
            default -> throw new IllegalStateException("(BUG) unhandled SubType (FeeDataType)");
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case DEFAULT -> "DEFAULT";
            case TOKEN_FUNGIBLE_COMMON -> "TOKEN_FUNGIBLE_COMMON";
            case TOKEN_NON_FUNGIBLE_UNIQUE -> "TOKEN_NON_FUNGIBLE_UNIQUE";
            case TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES -> "TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES";
            case TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES -> "TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES";
            case SCHEDULE_CREATE_CONTRACT_CALL -> "SCHEDULE_CREATE_CONTRACT_CALL";
        };
    }
}
