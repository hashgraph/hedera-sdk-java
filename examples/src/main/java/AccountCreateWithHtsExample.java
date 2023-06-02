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

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.*;
import java.util.concurrent.TimeoutException;

public final class AccountCreateWithHtsExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private AccountCreateWithHtsExample() {
    }

    public static void main(String[] args) throws NullPointerException, PrecheckStatusException, ReceiptStatusException, InterruptedException, TimeoutException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        client.setDefaultMaxTransactionFee(new Hbar(10));

        PrivateKey supplyKey = PrivateKey.generateECDSA();
        PrivateKey freezeKey = PrivateKey.generateECDSA();
        PrivateKey wipeKey = PrivateKey.generateECDSA();

        System.out.println("Example 1");

        // Step 1 - Create an NFT using the Hedera Token Service

        // IPFS content identifiers for the NFT metadata
        String[] cIDs = new String[] {
            "QmNPCiNA3Dsu3K5FxDPMG5Q3fZRwVTg14EXA92uqEeSRXn",
            "QmZ4dgAgt8owvnULxnKxNe8YqpavtVCXmc1Lt2XajFpJs9",
            "QmPzY5GxevjyfMUF5vEAjtyRoigzWp47MiKAtLBduLMC1T",
            "Qmd3kGgSrAwwSrhesYcY7K54f3qD7MDo38r7Po2dChtQx5",
            "QmWgkKz3ozgqtnvbCLeh7EaR1H8u5Sshx3ZJzxkcrT3jbw",
        };

        TokenCreateTransaction nftCreateTx = new TokenCreateTransaction()
            .setTokenName("HIP-542 Example Collection")
            .setTokenSymbol("HIP-542")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setDecimals(0)
            .setInitialSupply(0)
            .setMaxSupply(cIDs.length)
            .setTreasuryAccountId(OPERATOR_ID)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(OPERATOR_KEY)
            .setFreezeKey(freezeKey)
            .setWipeKey(wipeKey)
            .setSupplyKey(supplyKey)
            .freezeWith(client);

        // Sign the transaction with the operator key
        TokenCreateTransaction nftCreateTxSign = nftCreateTx.sign(OPERATOR_KEY);

        // Submit the transaction to the Hedera network
        TransactionResponse nftCreateSubmit = nftCreateTxSign.execute(client);

        // Get transaction receipt information
        TransactionReceipt nftCreateRx = nftCreateSubmit.getReceipt(client);
        TokenId nftTokenId = nftCreateRx.tokenId;
        System.out.println("Created NFT with token id: " + nftTokenId);

        // Step 2 -  Mint the NFT
        TransactionReceipt[] nftCollection = new TransactionReceipt[cIDs.length];
        for (int i = 0; i < cIDs.length; i++) {
            byte[] nftMetadata = cIDs[i].getBytes();
            TokenMintTransaction mintTx = new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .setMetadata(List.of(nftMetadata))
                .freezeWith(client);

            TokenMintTransaction mintTxSign = mintTx.sign(supplyKey);
            TransactionResponse mintTxSubmit = mintTxSign.execute(client);

            nftCollection[i] = mintTxSubmit.getReceipt(client);

            System.out.println("Created NFT " + nftTokenId + " with serial: " + nftCollection[i].serials.get(0));
        }

        long exampleNftId = nftCollection[0].serials.get(0);

        // Step 3 - Create an ECDSA public key alias
        System.out.println("Creating a new account...");

        PrivateKey privateKey = PrivateKey.generateECDSA();
        PublicKey publicKey = privateKey.getPublicKey();

        // Assuming that the target shard and realm are known.
        // For now they are virtually always 0 and 0.
        AccountId aliasAccountId = publicKey.toAccountId(0, 0);

        System.out.println("New account ID: " + aliasAccountId);
        System.out.println("Just the aliasKey: " + aliasAccountId.aliasKey);

        // Step 4 -  Tranfer the NFT to the public key alias using the transfer transaction
        TransferTransaction nftTransferTx = new TransferTransaction()
            .addNftTransfer(nftTokenId.nft(exampleNftId), OPERATOR_ID, aliasAccountId)
            .freezeWith(client);

        // Sign the transaction with the operator key
        TransferTransaction nftTransferTxSign = nftTransferTx.sign(OPERATOR_KEY);

        // Submit the transaction to the Hedera network
        TransactionResponse nftTransferSubmit = nftTransferTxSign.execute(client);

        // Get transaction receipt information here
        nftTransferSubmit.getReceipt(client);

        // Step 5 - Return the new account ID in the child record
        List<TokenNftInfo> nftInfo = new TokenNftInfoQuery()
            .setNftId(nftTokenId.nft(exampleNftId))
            .execute(client);

        String nftOwnerAccountId = nftInfo.get(0).accountId.toString();
        System.out.println("Current owner account id: " + nftOwnerAccountId);

        // Step 6 - Show the new account ID owns the NFT
        String accountId = new AccountInfoQuery()
            .setAccountId(aliasAccountId)
            .execute(client)
            .accountId.toString();

        System.out.println("The normal account ID of the given alias: " + accountId);

        if (nftOwnerAccountId.equals(accountId)) {
            System.out.println("The NFT owner accountId matches the accountId created with the HTS");
        } else {
            System.out.println("The two account IDs does not match");
        }


        System.out.println("Example 2");

        // Step 1 - Create a fungible HTS token using the Hedera Token Service
        TokenCreateTransaction tokenCreateTx = new TokenCreateTransaction()
            .setTokenName("HIP-542 Token")
            .setTokenSymbol("H542")
            .setTokenType(TokenType.FUNGIBLE_COMMON)
            .setTreasuryAccountId(OPERATOR_ID)
            .setInitialSupply(10000) // Total supply = 10000 / 10 ^ 2
            .setDecimals(2)
            .setAutoRenewAccountId(OPERATOR_ID)
            .freezeWith(client);

        // Sign the transaction with the operator key
        TokenCreateTransaction tokenCreateTxSign = tokenCreateTx.sign(OPERATOR_KEY);

        // Submit the transaction to the Hedera network
        TransactionResponse tokenCreateSubmit = tokenCreateTxSign.execute(client);

        // Get transaction receipt information
        TransactionReceipt tokenCreateRx = tokenCreateSubmit.getReceipt(client);
        TokenId tokenId = tokenCreateRx.tokenId;
        System.out.println("Created token with token id: " + tokenId);

        // Step 2 -  Create an ECDSA public key alias
        System.out.println("Creating a new account...");

        PrivateKey privateKey2 = PrivateKey.generateECDSA();
        PublicKey publicKey2 = privateKey2.getPublicKey();

        // Assuming that the target shard and realm are known.
        // For now they are virtually always 0 and 0.
        AccountId aliasAccountId2 = publicKey2.toAccountId(0, 0);

        System.out.println("New account ID: " + aliasAccountId2);
        System.out.println("Just the aliasKey: " + aliasAccountId2.aliasKey);

        // Step 3 -  Transfer the fungible token to the public key alias
        TransferTransaction tokenTransferTx = new TransferTransaction()
            .addTokenTransfer(tokenId, OPERATOR_ID, -10)
            .addTokenTransfer(tokenId, aliasAccountId2, 10)
            .freezeWith(client);

        // Sign the transaction with the operator key
        TransferTransaction tokenTransferTxSign = tokenTransferTx.sign(OPERATOR_KEY);

        // Submit the transaction to the Hedera network
        TransactionResponse tokenTransferSubmit = tokenTransferTxSign.execute(client);

        // Get transaction receipt information
        tokenTransferSubmit.getReceipt(client);

        // Step 4 -  Return the new account ID in the child record
        String accountId2 = new AccountInfoQuery()
            .setAccountId(aliasAccountId2)
            .execute(client)
            .accountId.toString();

        System.out.println("The normal account ID of the given alias: " + accountId2);

        // Step 5 -  Show the new account ID owns the fungible token
        AccountBalance accountBalances = new AccountBalanceQuery()
            .setAccountId(aliasAccountId2)
            .execute(client);

        int tokenBalanceAccountId2 = accountBalances.tokens.get(tokenId).intValue();
        if (tokenBalanceAccountId2 == 10) {
            System.out.println("Account is created successfully using HTS 'TransferTransaction'");
        } else {
            System.out.println("Creating account with HTS using public key alias failed");
        }

        client.close();
    }
}
