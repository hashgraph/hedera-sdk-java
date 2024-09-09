/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

abstract class PendingAirdropLogic<T extends PendingAirdropLogic<T>> extends Transaction<T> {

    protected List<PendingAirdropId> pendingAirdropIds = new ArrayList<>();


    protected PendingAirdropLogic() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException when there is an issue with the protobuf
     */
    PendingAirdropLogic(
        LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs)
        throws InvalidProtocolBufferException {
        super(txs);
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    PendingAirdropLogic(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
    }

    /**
     * Extract the pending airdrop ids
     *
     * @return the pending airdrop ids
     */
    public List<PendingAirdropId> getPendingAirdropIds() {
        return this.pendingAirdropIds;
    }

    /**
     * Set the pending airdrop ids
     *
     * @param pendingAirdropIds
     * @return {@code this}
     */
    public T setPendingAirdropIds(List<PendingAirdropId> pendingAirdropIds) {
        Objects.requireNonNull(pendingAirdropIds);
        requireNotFrozen();
        this.pendingAirdropIds = pendingAirdropIds;
        // noinspection unchecked
        return (T) this;
    }

    /**
     * clear the pending airdrop ids
     *
     * @return {@code this}
     */
    public T clearPendingAirdropIds() {
        requireNotFrozen();
        this.pendingAirdropIds = new ArrayList<>();
        // noinspection unchecked
        return (T) this;
    }

    /**
     * Add pendingAirdropId
     *
     * @param pendingAirdropId
     * @return {@code this}
     */
    public T addPendingAirdrop(PendingAirdropId pendingAirdropId) {
        Objects.requireNonNull(pendingAirdropId);
        requireNotFrozen();
        this.pendingAirdropIds.add(pendingAirdropId);
        // noinspection unchecked
        return (T) this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        for (var pendingAirdropId : pendingAirdropIds) {
            if (pendingAirdropId.getTokenId() != null) {
                pendingAirdropId.getTokenId().validateChecksum(client);
            }

            if (pendingAirdropId.getReceiver() != null) {
                pendingAirdropId.getReceiver().validateChecksum(client);
            }

            if (pendingAirdropId.getSender() != null) {
                pendingAirdropId.getSender().validateChecksum(client);
            }

            if (pendingAirdropId.getNftId() != null) {
                pendingAirdropId.getNftId().tokenId.validateChecksum(client);
            }
        }
    }
}
