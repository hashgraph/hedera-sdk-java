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

import javax.annotation.Nullable;
import java.util.List;

/**
 * Custom exception for when there are issues with the mnemonic.
 */
public class BadMnemonicException extends Exception {
    /**
     * The mnemonic that failed validation.
     */
    public final Mnemonic mnemonic;

    /**
     * The reason for which the mnemonic failed validation.
     */
    public final BadMnemonicReason reason;

    /**
     * If not null, these are the indices in the mnemonic that were not found in the
     * BIP-39 standard English word list.
     * <p>
     * If {@code reason == BadMnemonicReason.UnknownWords} then this will be not null.
     */
    @Nullable
    public final List<Integer> unknownWordIndices;

    /**
     * Constructor.
     *
     * @param mnemonic                  the mnemonic
     * @param reason                    the reason
     * @param unknownWordIndices        the indices
     */
    BadMnemonicException(Mnemonic mnemonic, BadMnemonicReason reason, List<Integer> unknownWordIndices) {
        this.mnemonic = mnemonic;
        this.reason = reason;
        this.unknownWordIndices = unknownWordIndices;
    }

    /**
     * Constructor.
     *
     * @param mnemonic                  the mnemonic
     * @param reason                    the reason
     */
    BadMnemonicException(Mnemonic mnemonic, BadMnemonicReason reason) {
        this.mnemonic = mnemonic;
        this.reason = reason;
        this.unknownWordIndices = null;
    }
}
