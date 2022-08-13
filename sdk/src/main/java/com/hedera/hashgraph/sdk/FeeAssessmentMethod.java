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

/**
 * Enum for the fee assessment method.
 * <p>
 * The terminology here (exclusive vs inclusive) is borrowed from tax assessment.
 */
public enum FeeAssessmentMethod {

    /**
     * If Alice is paying Bob, and an <b>inclusive</b> fractional fee is collected to be sent to Charlie,
     * the amount Alice declares she will pay in the transfer transaction <b>includes</b> the fee amount.
     * <p>
     * In other words, Bob receives the amount that Alice intended to send, minus the fee.
     */
    INCLUSIVE(false),
    /**
     * If Alice is paying Bob, and an <b>exclusive</b> fractional fee is collected to be sent to Charlie,
     * the amount Alice declares she will pay in the transfer transaction <b>does not</b> include the fee amount.
     * <p>
     * In other words, Alice is charged the fee <b>in addition to</b> the amount she intended to send to Bob.
     */
    EXCLUSIVE(true);

    final boolean code;

    FeeAssessmentMethod(boolean code) {
        this.code = code;
    }

    static FeeAssessmentMethod valueOf(boolean code) {
        return code ? EXCLUSIVE : INCLUSIVE;
    }

    @Override
    public String toString() {
        return code ? "EXCLUSIVE" : "INCLUSIVE";
    }
}
