/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2023 Hedera Hashgraph, LLC
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
 * Custom exception thrown by the entity helper validate method when the account id and checksum are invalid.
 */
public class BadEntityIdException extends Exception {
    /**
     * the shard portion of the account id
     */
    public final long shard;
    /**
     * the realm portion of the account id
     */
    public final long realm;
    /**
     * the num portion of the account id
     */
    public final long num;
    /**
     * the user supplied checksum
     */
    public final String presentChecksum;
    /**
     * the calculated checksum
     */
    public final String expectedChecksum;

    /**
     * Constructor.
     *
     * @param shard                     the shard portion of the account id
     * @param realm                     the realm portion of the account id
     * @param num                       the num portion of the account id
     * @param presentChecksum           the user supplied checksum
     * @param expectedChecksum          the calculated checksum
     */
    BadEntityIdException(long shard, long realm, long num, String presentChecksum, String expectedChecksum) {
        super(String.format("Entity ID %d.%d.%d-%s was incorrect.", shard, realm, num, presentChecksum));
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.presentChecksum = presentChecksum;
        this.expectedChecksum = expectedChecksum;
    }
}
