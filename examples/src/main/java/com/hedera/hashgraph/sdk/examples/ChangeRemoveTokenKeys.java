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
package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * HIP-540: Change Or Remove Existing Keys From A Token.
 * All entities across Hedera have opt-in administrative keys (or simply admin keys).
 * Currently, the Consensus Service and File service allow these keys to be removed
 * by an update that sets them to an empty KeyList, which is a sentinel value for immutability.
 * However the Hedera Token Service does not provide such a feature consistently.
 * We should enable existing admin keys for tokens created with the Hedera Token Service
 * to be able to sign an update transaction that changes or permanently removes any key
 * (Admin, Wipe, KYC, Freeze, Pause, Supply, Fee Schedule, Metadata) from the token.
 */
class ChangeRemoveTokenKeys {

    // See `.env.sample` in the `examples` folder root for how to specify values below
    // or set environment variables with the same names.

    // Operator's account ID.
    // Used to sign and pay for operations on Hedera.
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    // Operator's private key.
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // `HEDERA_NETWORK` defaults to `testnet` if not specified in dotenv file
    // Networks can be: `localhost`, `testnet`, `previewnet`, `mainnet`.
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    // `SDK_LOG_LEVEL` defaults to `SILENT` if not specified in dotenv file
    // Log levels can be: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `SILENT`.
    // Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL,
    // for example via VM options: `-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace`
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        /*
         * Step 1:
         * Generate keys for future token.
         */
        PrivateKey adminPrivateKey = PrivateKey.generateED25519();
        PublicKey adminPublicKey = adminPrivateKey.getPublicKey();

        PrivateKey supplyPrivateKey = PrivateKey.generateED25519();
        PublicKey supplyPublicKey = supplyPrivateKey.getPublicKey();

        PrivateKey newSupplyPrivateKey = PrivateKey.generateED25519();
        PublicKey newSupplyPublicKey = newSupplyPrivateKey.getPublicKey();

        PrivateKey wipePrivateKey = PrivateKey.generateED25519();
        PublicKey wipePublicKey = wipePrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create a non-fungible token and check its keys.
         */
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("Example NFT")
                .setTokenSymbol("ENFT")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(OPERATOR_ID)
                .setAdminKey(adminPublicKey)
                .setWipeKey(wipePublicKey)
                .setSupplyKey(supplyPublicKey)
                .freezeWith(client)
                .sign(adminPrivateKey)
                .execute(client)
                .getReceipt(client)
                .tokenId
        );

        var tokenInfoBefore = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        if (tokenInfoBefore.adminKey != null &&
            tokenInfoBefore.supplyKey != null &&
            tokenInfoBefore.wipeKey != null) {
            System.out.println("Admin Key:" + tokenInfoBefore.adminKey);
            System.out.println("Supply Key:" + tokenInfoBefore.supplyKey);
            System.out.println("Wipe Key:" + tokenInfoBefore.wipeKey);
        } else {
            throw new Exception("The required keys are not set correctly.");
        }

        /*
         * Step 3:
         * Remove Wipe Key from a token and check that its removed.
         */
        System.out.println("Removing Wipe Key...");

        // This HIP introduces ability to remove lower-privilege keys
        // (Wipe, KYC, Freeze, Pause, Supply, Fee Schedule, Metadata) from a Token
        // using an update with the empty KeyList.
        var emptyKeyList = new KeyList();

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setWipeKey(emptyKeyList)
            // it is set by default, but we set it here explicitly for illustration
            .setKeyVerificationMode(TokenKeyValidation.FULL_VALIDATION)
            .freezeWith(client)
            .sign(adminPrivateKey)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterWipeKeyRemoval = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        if (tokenInfoAfterWipeKeyRemoval.wipeKey == null) {
            System.out.println("Wipe Key (after removal):" + tokenInfoAfterWipeKeyRemoval.wipeKey);
        } else {
            throw new Exception("Wipe key was not removed after removal operation.");
        }

        /*
         * Step 4:
         * Remove Admin Key from a token and check that its removed.
         */
        System.out.println("Removing Admin Key...");

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setAdminKey(emptyKeyList)
            .setKeyVerificationMode(TokenKeyValidation.NO_VALIDATION)
            .freezeWith(client)
            .sign(adminPrivateKey)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterAdminKeyRemoval = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        if (tokenInfoAfterAdminKeyRemoval.adminKey == null) {
            System.out.println("Admin Key (after removal):" + tokenInfoAfterAdminKeyRemoval.adminKey);
        } else {
            throw new Exception("Admin key was not removed after removal operation.");
        }

        /*
         * Step 5:
         * Update Supply Key and check that its updated.
         */
        System.out.println("Updating Supply Key...");

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setSupplyKey(newSupplyPublicKey)
            .setKeyVerificationMode(TokenKeyValidation.FULL_VALIDATION)
            .freezeWith(client)
            .sign(supplyPrivateKey)
            .sign(newSupplyPrivateKey)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterSupplyKeyUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        if (tokenInfoAfterSupplyKeyUpdate.supplyKey.equals(newSupplyPublicKey)) {
            System.out.println("Supply Key (after update):" + tokenInfoAfterSupplyKeyUpdate.supplyKey);
        } else {
            throw new Exception("Supply key was not updated correctly.");
        }

        /*
         * Step 6:
         * Remove Supply Key (update to unusable key).
         */
        System.out.println("Removing Supply Key...");

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setSupplyKey(PublicKey.unusableKey())
            .setKeyVerificationMode(TokenKeyValidation.NO_VALIDATION)
            .freezeWith(client)
            .sign(newSupplyPrivateKey)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterSupplyKeyRemoval = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        var supplyKeyAfterRemoval = (PublicKey) tokenInfoAfterSupplyKeyRemoval.supplyKey;

        if (supplyKeyAfterRemoval.equals(PublicKey.unusableKey())) {
            System.out.println("Supply Key (after removal):" + supplyKeyAfterRemoval.toStringRaw());
        } else {
            throw new Exception("Supply key was not removed after removal operation.");
        }

        /*
         * Clean up:
         * Can't delete a token as it is immutable.
         */
        client.close();

        System.out.println("Example complete!");
    }
}
