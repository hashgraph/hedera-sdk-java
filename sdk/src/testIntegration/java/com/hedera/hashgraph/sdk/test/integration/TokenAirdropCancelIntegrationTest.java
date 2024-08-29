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
package com.hedera.hashgraph.sdk.test.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenAirdropCancelIntegrationTest {

    @Test
    @DisplayName("Cancels the tokens when they are in pending state")
    void canCancelTokens() {
        // create fungible and nf token
        // mint some NFTs

        // create receiver with 0 auto associations

        // airdrop the tokens
        // cancel the tokens with the receiver

        // verify in the transaction record the pending airdrop ids for nft and ft - should no longer exist
        // verify the receiver does not hold the tokens via query
        // verify the operator holds the tokens
    }

    @Test
    @DisplayName("Cancels the tokens when token is frozen")
    void canCancelTokensWhenTokenIsFrozen() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // freeze the token

        // cancel the tokens with the receiver
    }

    @Test
    @DisplayName("Cancels the tokens when token is paused")
    void canCancelTokensWhenTokenIsPaused() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // pause the token

        // cancel the tokens with the receiver
    }

    @Test
    @DisplayName("Cancels the tokens when token is deleted")
    void canCancelTokensWhenTokenIsDeleted() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // delete the token

        // cancel the tokens with the receiver
    }

    @Test
    @DisplayName("Cancels the tokens when they are in pending state to multiple receivers")
    void canCancelTokensToMultipleReceivers() {
        // create fungible and nf token
        // mint some NFTs

        // create receiver1 with 0 auto associations
        // create receiver2 with 0 auto associations

        // airdrop the tokens to both

        // cancel the tokens signing with receiver1 and receiver2

        // verify in the transaction record the pending airdrop ids for nft and ft - should no longer exist
        // verify receiver1 holds the tokens via query
        // verify receiver2 holds the tokens via query
        // verify the operator does not hold the tokens
    }


    @Test
    @DisplayName("Cancels the tokens when they are in pending state from multiple airdrop transactions")
    void canCancelTokensFromMultipleAirdropTxns() {
        // create fungible and nf token
        // mint some NFTs

        // create receiver with 0 auto associations

        // airdrop some of the tokens to the receiver
        // airdrop some of the tokens to the receiver
        // airdrop some of the tokens to the receiver

        // get the PendingIds from the records

        // cancel the all the tokens with the receiver

        // verify in the transaction record the pending airdrop ids for nft and ft - should no longer exist
        // verify receiver holds the tokens via query
        // verify the operator does not hold the tokens
    }

    @Test
    @DisplayName("Cancels the tokens when they are in pending state from multiple airdrop transactions")
    void canCancelTokensFromHollowAccount() {
        // create fungible and nf token
        // mint some NFTs

        // airdrop the tokens to an alias
        // should lazy-create and transfer the tokens

        // cancel the tokens signing with the key of the hollow account

        // verify in the transaction record the pending airdrop ids for nft and ft - should no longer exist
        // verify receiver holds the tokens via query
        // verify the operator does not hold the tokens
    }

    @Test
    @DisplayName("Cannot cancel the tokens when they are not airdropped")
    void cannotCancelTokensForNonExistingAirdrop() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // cancel the tokens with the operator which does not have pending airdrops
        // fails
    }

    @Test
    @DisplayName("Cannot cancel the tokens when they are already canceled")
    void canonCancelTokensForAlreadyCanceledAirdrop() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // cancel the tokens with the receiver

        // cancel the tokens with the receiver again
        // fails
    }


    @Test
    @DisplayName("Cannot cancel the tokens with empty list")
    void canonCancelWithEmptyPendingAirdropsList() {
        // create fungible token

        // create receiver with 0 auto associations

        // cancel the tokens with the receiver without setting pendingAirdropIds
        // fails
    }

    @Test
    @DisplayName("Cannot cancel the tokens with duplicate entries")
    void cannotCancelTokensWithDuplicateEntries() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // cancel the tokens with duplicate pending airdrop token ids
        // fails
    }
}
