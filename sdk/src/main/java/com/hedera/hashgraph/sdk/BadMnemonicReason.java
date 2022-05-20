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
 * Possible reason why a {@link Mnemonic} failed validation.
 */
public enum BadMnemonicReason {
    /**
     * The mnemonic did not contain exactly 24 words.
     */
    BadLength,

    /**
     * The mnemonic contained words which were not found in the BIP-39 standard English word list.
     * <p>
     * {@link BadMnemonicException#unknownWordIndices} will be set with the list of word indices
     * in {@link Mnemonic#words} which were not found in the standard word list.
     *
     * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0039/english.txt">BIP-39
     * English word list</a>.
     */
    UnknownWords,

    /**
     * The checksum encoded in the mnemonic did not match the checksum we just calculated for
     * that mnemonic.
     * <p>
     * 24-word mnemonics have an 8-bit checksum that is appended to the 32 bytes of source entropy
     * after being calculated from it, before being encoded into words. This status is returned if
     * {@link Mnemonic#validate()} calculated a different checksum for the mnemonic than that which
     * was encoded into it.
     * <p>
     * This could happen if two or more of the words were entered out of the original order or
     * replaced with another from the standard word list (as this is only returned if all the words
     * exist in the word list).
     */
    ChecksumMismatch,
    /**
     * The given mnemonic doesn't contain 22 words required to be a legacy mnemonic, or the words are
     * not in the legacy list.
     */
    NotLegacy
}
