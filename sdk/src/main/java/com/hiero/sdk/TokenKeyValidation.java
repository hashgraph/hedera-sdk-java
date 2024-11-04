/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk;

/**
 * Types of validation strategies for token keys.
 *
 */
public enum TokenKeyValidation {
    /**
     * Currently the default behaviour. It will perform all token key validations.
     */
    FULL_VALIDATION(com.hiero.sdk.proto.TokenKeyValidation.FULL_VALIDATION),

    /**
     * Perform no validations at all for all passed token keys.
     */
    NO_VALIDATION(com.hiero.sdk.proto.TokenKeyValidation.NO_VALIDATION);

    final com.hiero.sdk.proto.TokenKeyValidation code;

    /**
     * Constructor.
     *
     * @param code the token key validation
     */
    TokenKeyValidation(com.hiero.sdk.proto.TokenKeyValidation code) {
        this.code = code;
    }

    static TokenKeyValidation valueOf(com.hiero.sdk.proto.TokenKeyValidation code) {
        return switch (code) {
            case FULL_VALIDATION -> FULL_VALIDATION;
            case NO_VALIDATION -> NO_VALIDATION;
            default -> throw new IllegalStateException("(BUG) unhandled TokenKeyValidation");
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case FULL_VALIDATION -> "FULL_VALIDATION";
            case NO_VALIDATION -> "NO_VALIDATION";
        };
    }

    public com.hiero.sdk.proto.TokenKeyValidation toProtobuf() {
        return this.code;
    }
}
