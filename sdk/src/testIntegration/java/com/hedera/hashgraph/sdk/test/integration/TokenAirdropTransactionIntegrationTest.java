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

class TokenAirdropTransactionIntegrationTest {
    @Test
    @DisplayName("Transfers tokens when the account is associated")
    void canAirdropAssociatedTokens() {
        // create fungible and nf token
        // mint some NFTs

        // create receiver with unlimited auto associations and receiverSig = false

        // airdrop the tokens

        // verify the receiver holds the tokens via query
        // verify the operator does not hold the tokens
    }

    @Test
    @DisplayName("Tokens are in pending state when the account is not associated")
    void canAirdropNonAssociatedTokens() {
        // create fungible and nf token
        // mint some NFTs

        // create receiver with no auto associations and receiverSig = false

        // airdrop the tokens

        // verify in the transaction record the pending airdrops
        // verify the receiver does not hold the tokens via query
        // verify the operator does hold the tokens
    }

    @Test
    @DisplayName("Airdrop creates a hollow account and transfers the tokens")
    void canAirdropToAlias() {
        // create fungible and nf token
        // mint some NFTs

        // airdrop the tokens to an alias
        // should lazy-create and transfer the tokens

        // verify the receiver holds the tokens via query
        // verify the operator does not hold the tokens
    }


    @Test
    @DisplayName("Can airdrop with custom fees")
    void canAirdropWithCustomFee() {
        // create fungible token with custom fee another token
        // make the custom fee to be paid by the sender and the fee collector to be the operator account

        // create sender account with unlimited associations and send some tokens to it
        // create receiver with unlimited associations

        // airdrop the tokens from the sender

        // verify the custom fee has been paid by the sender to the collector
    }

    @Test
    @DisplayName("Cannot airdrop ft with receiverSig=true")
    void cannotAirdropTokensWithReceiverSigRequiredFungible() {
        // create fungible token

        // create receiver with unlimited auto associations and receiverSig = true

        // airdrop the tokens
        // fails
    }

    @Test
    @DisplayName("Cannot airdrop nft with receiverSig=true")
    void cannotAirdropTokensWithReceiverSigRequiredNFT() {
        // create nft
        // mint some tokens

        // create receiver with unlimited auto associations and receiverSig = true

        // airdrop the tokens
        // fails
    }

    @Test
    @DisplayName("Cannot airdrop ft with no balance")
    void cannotAirdropTokensWithAllowanceAndWithoutBalanceFungible() {
        // create fungible token
        // create spender and approve to it some tokens

        // create receiver with unlimited auto associations

        // airdrop the tokens from the sender to the receiver
        // fails
    }

    @Test
    @DisplayName("Cannot airdrop nft with no balance")
    void cannotAirdropTokensWithAllowanceAndWithoutBalanceNFT() {
        // create nft
        // mint som tokens
        // create spender and approve to it some tokens

        // create receiver with unlimited auto associations

        // airdrop the tokens from the sender to the receiver
        // fails
    }

    @Test
    @DisplayName("Cannot airdrop with invalid body")
    void cannotAirdropTokensWithInvalidBody() {
        // airdrop with no tokenID or NftID
        // fails

        // airdrop with no accountID
        // fails

        // airdrop with more than 10 tokenIDs/nftIDs
        // fails
    }
}
