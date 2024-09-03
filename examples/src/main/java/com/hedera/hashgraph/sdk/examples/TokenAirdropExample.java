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
package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenAirdropTransaction;
import com.hedera.hashgraph.sdk.TokenCancelAirdropTransaction;
import com.hedera.hashgraph.sdk.TokenClaimAirdropTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenRejectTransaction;
import com.hedera.hashgraph.sdk.TokenSupplyType;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TokenAirdropExample {

    /*
     * See .env.sample in the examples folder root for how to specify values below
     * or set environment variables with the same names.
     */

    /**
     * Operator's account ID. Used to sign and pay for operations on Hedera.
     */
    private static final AccountId OPERATOR_ID = AccountId.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    /**
     * Operator's private key.
     */
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    /**
     * HEDERA_NETWORK defaults to testnet if not specified in dotenv file. Network can be: localhost, testnet,
     * previewnet or mainnet.
     */
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    /**
     * SDK_LOG_LEVEL defaults to SILENT if not specified in dotenv file. Log levels can be: TRACE, DEBUG, INFO, WARN,
     * ERROR, SILENT.
     * <p>
     * Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL, for example via VM
     * options: -Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace
     */
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        System.out.println("Example Start!");

        /*
         * Step 0:
         * Create and configure SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        /*
         * Step 1:
         * Create 4 accounts
         */
        var privateKey1 = PrivateKey.generateECDSA();
        var account1 = new AccountCreateTransaction()
            .setKey(privateKey1)
            .setInitialBalance(new Hbar(10))
            .setMaxAutomaticTokenAssociations(-1)
            .execute(client)
            .getReceipt(client)
            .accountId;

        var privateKey2 = PrivateKey.generateECDSA();
        var account2 = new AccountCreateTransaction()
            .setKey(privateKey2)
            .setMaxAutomaticTokenAssociations(1)
            .execute(client)
            .getReceipt(client)
            .accountId;

        var privateKey3 = PrivateKey.generateECDSA();
        var account3 = new AccountCreateTransaction()
            .setKey(privateKey3)
            .setMaxAutomaticTokenAssociations(0)
            .execute(client)
            .getReceipt(client)
            .accountId;

        var privateKey4 = PrivateKey.generateECDSA();
        var treasuryAccount = new AccountCreateTransaction()
            .setKey(privateKey4)
            .setInitialBalance(new Hbar(10))
            .execute(client)
            .getReceipt(client)
            .accountId;

        /*
         * Step 2:
         * Create FT and NFT and mint
         */
        var tokenID = new TokenCreateTransaction()
            .setTokenName("Fungible Token")
            .setTokenSymbol("TFT")
            .setTokenMemo("Example memo")
            .setDecimals(3)
            .setInitialSupply(100)
            .setMaxSupply(100)
            .setTreasuryAccountId(treasuryAccount)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(client.getOperatorPublicKey())
            .setFreezeKey(client.getOperatorPublicKey())
            .setSupplyKey(client.getOperatorPublicKey())
            .setMetadataKey(client.getOperatorPublicKey())
            .setPauseKey(client.getOperatorPublicKey())
            .freezeWith(client)
            .sign(privateKey4)
            .execute(client)
            .getReceipt(client)
            .tokenId;

        var nftID = new TokenCreateTransaction()
            .setTokenName("Test NFT")
            .setTokenSymbol("TNFT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(treasuryAccount)
            .setSupplyType(TokenSupplyType.FINITE)
            .setMaxSupply(10)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(client.getOperatorPublicKey())
            .setFreezeKey(client.getOperatorPublicKey())
            .setSupplyKey(client.getOperatorPublicKey())
            .setMetadataKey(client.getOperatorPublicKey())
            .setPauseKey(client.getOperatorPublicKey())
            .freezeWith(client)
            .sign(privateKey4)
            .execute(client)
            .getReceipt(client)
            .tokenId;

        new TokenMintTransaction()
            .setTokenId(nftID)
            .setMetadata(generateNftMetadata((byte) 3))
            .execute(client)
            .getReceipt(client);


        /*
         * Step 3:
         * Airdrop fungible tokens to all 3 accounts
         */
        System.out.println("Airdropping fts");
        var txnRecord = new TokenAirdropTransaction()
            .addTokenTransfer(tokenID, account1, 10)
            .addTokenTransfer(tokenID, treasuryAccount, -10)
            .addTokenTransfer(tokenID, account2, 10)
            .addTokenTransfer(tokenID, treasuryAccount, -10)
            .addTokenTransfer(tokenID, account3, 10)
            .addTokenTransfer(tokenID, treasuryAccount, -10)
            .freezeWith(client)
            .sign(privateKey4)
            .execute(client)
            .getRecord(client);

        /*
         * Step 4:
         * Get the transaction record and see one pending airdrop (for Account 3)
         */
        System.out.println("Pending airdrops length: " + txnRecord.pendingAirdropRecords.size());
        System.out.println("Pending airdrops: " + txnRecord.pendingAirdropRecords.get(0));

        /*
         * Step 5:
         * Query to verify Account 1 and Account 2 received the airdrops and Account 3 did not
         */
        var account1Balance = new AccountBalanceQuery()
            .setAccountId(account1)
            .execute(client);
        var account2Balance = new AccountBalanceQuery()
            .setAccountId(account2)
            .execute(client);
        var account3Balance = new AccountBalanceQuery()
            .setAccountId(account3)
            .execute(client);

        System.out.println("Account1 ft balance after airdrop: " + account1Balance.tokens.get(tokenID));
        System.out.println("Account2 ft balance after airdrop: " + account2Balance.tokens.get(tokenID));
        System.out.println("Account3 ft balance after airdrop: " + account3Balance.tokens.get(tokenID));

        /*
         * Step 6:
         * Claim the airdrop for Account 3
         */
        System.out.println("Claiming ft with account3");
        new TokenClaimAirdropTransaction()
            .addPendingAirdrop(txnRecord.pendingAirdropRecords.get(0).getPendingAirdropId())
            .freezeWith(client)
            .sign(privateKey3)
            .execute(client)
            .getReceipt(client);

        account3Balance = new AccountBalanceQuery()
            .setAccountId(account3)
            .execute(client);
        System.out.println("Account3 ft balance after claim: " + account3Balance.tokens.get(tokenID));

        /*
         * Step 7:
         * Airdrop the NFTs to all three accounts
         */
        System.out.println("Airdropping nfts");
        txnRecord = new TokenAirdropTransaction()
            .addNftTransfer(nftID.nft(1), treasuryAccount, account1)
            .addNftTransfer(nftID.nft(2), treasuryAccount, account2)
            .addNftTransfer(nftID.nft(3), treasuryAccount, account3)
            .freezeWith(client)
            .sign(privateKey4)
            .execute(client)
            .getRecord(client);

        /*
         * Step 8:
         * Get the transaction record and verify two pending airdrops (for Account 2 & 3)
         */
        System.out.println("Pending airdrops length: " + txnRecord.pendingAirdropRecords.size());
        System.out.println("Pending airdrops for account 2: " + txnRecord.pendingAirdropRecords.get(0));
        System.out.println("Pending airdrops for account 3: " + txnRecord.pendingAirdropRecords.get(1));

        /*
         * Step 9:
         * Query to verify Account 1 received the airdrop and Account 2 and Account 3 did not
         */
        account1Balance = new AccountBalanceQuery()
            .setAccountId(account1)
            .execute(client);
        account2Balance = new AccountBalanceQuery()
            .setAccountId(account2)
            .execute(client);
        account3Balance = new AccountBalanceQuery()
            .setAccountId(account3)
            .execute(client);

        System.out.println("Account1 nft balance after airdrop: " + account1Balance.tokens.get(nftID));
        System.out.println("Account2 nft balance after airdrop: " + account2Balance.tokens.get(nftID));
        System.out.println("Account3 nft balance after airdrop: " + account3Balance.tokens.get(nftID));

        /*
         * Step 10:
         * Claim the airdrop for Account 2
         */
        System.out.println("Claiming nft with account2");
        new TokenClaimAirdropTransaction()
            .addPendingAirdrop(txnRecord.pendingAirdropRecords.get(0).getPendingAirdropId())
            .freezeWith(client)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        account2Balance = new AccountBalanceQuery()
            .setAccountId(account2)
            .execute(client);
        System.out.println("Account2 nft balance after claim: " + account2Balance.tokens.get(nftID));

        /*
         * Step 11:
         * Cancel the airdrop for Account 3
         */
        System.out.println("Canceling nft for account3");
        new TokenCancelAirdropTransaction()
            .addPendingAirdrop(txnRecord.pendingAirdropRecords.get(1).getPendingAirdropId())
            .freezeWith(client)
            .sign(privateKey4)
            .execute(client)
            .getReceipt(client);

        account3Balance = new AccountBalanceQuery()
            .setAccountId(account3)
            .execute(client);
        System.out.println("Account3 nft balance after cancel: " + account3Balance.tokens.get(nftID));

        /*
         * Step 12:
         * Reject the NFT for Account 2
         */
        System.out.println("Rejecting nft with account2");
        new TokenRejectTransaction()
            .setOwnerId(account2)
            .addNftId(nftID.nft(2))
            .freezeWith(client)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 13:
         * Query to verify Account 2 no longer has the NFT
         */
        account2Balance = new AccountBalanceQuery()
            .setAccountId(account2)
            .execute(client);
        System.out.println("Account2 nft balance after reject: " + account2Balance.tokens.get(nftID));

        /*
         * Step 13:
         * Query to verify the NFT was returned to the Treasury
         */
        var treasuryBalance = new AccountBalanceQuery()
            .setAccountId(treasuryAccount)
            .execute(client);
        System.out.println("Treasury nft balance after reject: " + treasuryBalance.tokens.get(nftID));

        /*
         * Step 14:
         * Reject the fungible tokens for Account 3
         */
        System.out.println("Rejecting ft with account3");
        new TokenRejectTransaction()
            .setOwnerId(account3)
            .addTokenId(tokenID)
            .freezeWith(client)
            .sign(privateKey3)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 14:
         * Query to verify Account 3 no longer has the fungible tokens
         */
        account3Balance = new AccountBalanceQuery()
            .setAccountId(account3)
            .execute(client);
        System.out.println("Account3 ft balance after reject: " + account3Balance.tokens.get(tokenID));

        /*
         * Step 15:
         * Query to verify Treasury received the rejected fungible tokens
         */
        treasuryBalance = new AccountBalanceQuery()
            .setAccountId(treasuryAccount)
            .execute(client);
        System.out.println("Treasury ft balance after reject: " + treasuryBalance.tokens.get(tokenID));

        /*
         * Clean up:
         */
        client.close();

        System.out.println("Example Complete!");
    }

    private static List<byte[]> generateNftMetadata(byte metadataCount) {
        List<byte[]> metadatas = new ArrayList<>();

        for (byte i = 0; i < metadataCount; i++) {
            byte[] md = {i};
            metadatas.add(md);
        }

        return metadatas;
    }

}
