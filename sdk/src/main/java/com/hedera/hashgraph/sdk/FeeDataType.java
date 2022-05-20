/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.SubType;

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
    TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES(SubType.TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES);


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
            case TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES:
                return TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES;
            case TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES:
                return TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES;
            default:
                throw new IllegalStateException("(BUG) unhandled SubType (FeeDataType)");
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case DEFAULT:
                return "DEFAULT";
            case TOKEN_FUNGIBLE_COMMON:
                return "TOKEN_FUNGIBLE_COMMON";
            case TOKEN_NON_FUNGIBLE_UNIQUE:
                return "TOKEN_NON_FUNGIBLE_UNIQUE";
            case TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES:
                return "TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES";
            case TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES:
                return "TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES";
            default:
                return "<UNRECOGNIZED VALUE>";
        }
    }
}
