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

class TokenAirdropClaimIntegrationTest {
    @Test
    @DisplayName("Claims the tokens when they are in pending state")
    void canClaimTokens() {
        // create fungible and nf token
        // mint some NFTs

        // create receiver with 0 auto associations

        // airdrop the tokens

        // claim the tokens with the receiver

        // verify in the transaction record the pending airdrop ids for nft and ft - should no longer exist
        // verify the receiver holds the tokens via query
        // verify the operator does not hold the tokens
    }

    @Test
    @DisplayName("Claims the tokens when they are in pending state to multiple receivers")
    void canClaimTokensToMultipleReceivers() {
        // create fungible and nf token
        // mint some NFTs

        // create receiver1 with 0 auto associations
        // create receiver2 with 0 auto associations

        // airdrop the tokens to both

        // claim the tokens signing with receiver1 and receiver2

        // verify in the transaction record the pending airdrop ids for nft and ft - should no longer exist
        // verify receiver1 holds the tokens via query
        // verify receiver2 holds the tokens via query
        // verify the operator does not hold the tokens
    }


    @Test
    @DisplayName("Claims the tokens when they are in pending state from multiple airdrop transactions")
    void canClaimTokensFromMultipleAirdropTxns() {
        // create fungible and nf token
        // mint some NFTs

        // create receiver with 0 auto associations

        // airdrop some of the tokens to the receiver
        // airdrop some of the tokens to the receiver
        // airdrop some of the tokens to the receiver

        // get the PendingIds from the records

        // claim the all the tokens with the receiver

        // verify in the transaction record the pending airdrop ids for nft and ft - should no longer exist
        // verify receiver holds the tokens via query
        // verify the operator does not hold the tokens
    }

    @Test
    @DisplayName("Claims the tokens when they are in pending state from multiple airdrop transactions")
    void canClaimTokensFromHollowAccount() {
        // create fungible and nf token
        // mint some NFTs

        // airdrop the tokens to an alias
        // should lazy-create and transfer the tokens

        // claim the tokens signing with the key of the hollow account

        // verify in the transaction record the pending airdrop ids for nft and ft - should no longer exist
        // verify receiver holds the tokens via query
        // verify the operator does not hold the tokens
    }

    @Test
    @DisplayName("Cannot claim the tokens when they are not airdropped")
    void canonClaimTokensForNonExistingAirdrop() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // claim the tokens with the operator which does not have pending airdrops
        // fails
    }

    @Test
    @DisplayName("Cannot claim the tokens when they are already claimed")
    void canonClaimTokensForAlreadyClaimedAirdrop() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // claim the tokens with the receiver

        // claim the tokens with the receiver again
        // fails
    }

    @Test
    @DisplayName("Cannot claim the tokens with empty list")
    void canonClaimWithEmptyPendingAirdropsList() {
        // create fungible token

        // create receiver with 0 auto associations

        // claim the tokens with the receiver without setting pendingAirdropIds
        // fails
    }

    @Test
    @DisplayName("Cannot claim the tokens with duplicate entries")
    void cannotClaimTokensWithDuplicateEntries() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // claim the tokens with duplicate pending airdrop token ids
        // fails
    }

    @Test
    @DisplayName("Cannot claim the tokens when token is paused")
    void cannotClaimTokensWhenTokenIsPaused() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // pause the token

        // claim the tokens with receiver
        // fails
    }

    @Test
    @DisplayName("Cannot claim the tokens when token is deleted")
    void cannotClaimTokensWhenTokenIsDeleted() {
        // create fungible token

        // create receiver with 0 auto associations

        // airdrop the tokens

        // delete the token

        // claim the tokens with receiver
        // fails
    }

}
