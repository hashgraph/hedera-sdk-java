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
import java.util.List;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/*
    Example for HIP-336.
    ### Show functionalities around approve/delete an allowance for:
    1. single NFT serial numbers
    2. all serial numbers at once
    3. delegating spender obligations
    Note that the concept around the ERC standard that Hedera implements in regard
    to the allowances for NFTs does not allow users to:
    1. approve allowance for all serials in a NFT collection, then remove allowance for individual serial of the NFT
    2. approve allowance for individual serial of the NFT, then remove allowance for all serials in the NFT collection
*/
public final class NftAddRemoveAllowancesExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private NftAddRemoveAllowancesExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Example 1
        System.out.println("Example 1: Approve/delete allowances for single serial numbers");

        String[] CIDs = {
            "QmNPCiNA3Dsu3K5FxDPMG5Q3fZRwVTg14EXA92uqEeSRXn",
            "QmZ4dgAgt8owvnULxnKxNe8YqpavtVCXmc1Lt2XajFpJs9",
            "QmPzY5GxevjyfMUF5vEAjtyRoigzWp47MiKAtLBduLMC1T",
        };

        TransactionReceipt nftCreateReceipt = new TokenCreateTransaction()
            .setTokenName("NFT Token")
            .setTokenSymbol("NFTT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setDecimals(0)
            .setInitialSupply(0)
            .setMaxSupply(CIDs.length)
            .setTreasuryAccountId(OPERATOR_ID)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(OPERATOR_KEY)
            .setSupplyKey(OPERATOR_KEY)
            .freezeWith(client)
            .execute(client)
            .getReceipt(client);

        TokenId nftTokenId = nftCreateReceipt.tokenId;
        System.out.println("Created NFT with token id: " + nftTokenId);

        List<TransactionReceipt> nftCollection = new ArrayList<>();
        for (int i = 0; i < CIDs.length; i++) {
            nftCollection.add(new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .setMetadata(List.of(CIDs[i].getBytes(StandardCharsets.UTF_8)))
                .freezeWith(client)
                .execute(client)
                .getReceipt(client));

            System.out.println("Created NFT " + nftTokenId + " with serial: " + nftCollection.get(i).serials.get(0));
        }

        // Create spender account
        PrivateKey spenderKey = PrivateKey.generateECDSA();
        AccountId spenderAccountId = new AccountCreateTransaction()
            .setKey(spenderKey)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("spenderAccountId: " + spenderAccountId);

        // Create receiver account
        PrivateKey receiverKey = PrivateKey.generateECDSA();
        AccountId receiverAccountId = new AccountCreateTransaction()
            .setKey(receiverKey)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("receiverAccountId: " + receiverAccountId);

        // Associate the spender with the NFT
        TransactionReceipt spenderAssociateReceipt = new TokenAssociateTransaction()
            .setAccountId(spenderAccountId)
            .setTokenIds(List.of(nftTokenId))
            .freezeWith(client)
            .sign(spenderKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Spender associate TX status: " + spenderAssociateReceipt.status);

        // Associate the receiver with the NFT
        TransactionReceipt receiverAssociateReceipt = new TokenAssociateTransaction()
            .setAccountId(receiverAccountId)
            .setTokenIds(List.of(nftTokenId))
            .freezeWith(client)
            .sign(receiverKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Receiver associate TX status: " + receiverAssociateReceipt.status);

        NftId nft1 = new NftId(nftTokenId, 1);
        NftId nft2 = new NftId(nftTokenId, 2);

        TransactionReceipt approveReceipt = new AccountAllowanceApproveTransaction()
            .approveTokenNftAllowance(nft1, OPERATOR_ID, spenderAccountId)
            .approveTokenNftAllowance(nft2, OPERATOR_ID, spenderAccountId)
            .execute(client)
            .getReceipt(client);
        System.out.println("Approve spender allowance for serials 1 and 2 - status: " + approveReceipt.status);

        // Generate TransactionId from spender's account id in order
        // for the transaction to be executed on behalf of the spender
        TransactionId onBehalfOfTransactionId = TransactionId.generate(spenderAccountId);

        // Sending NFT with serial number 1
        // `Spender` has an allowance to send, should end up with `SUCCESS`
        TransactionReceipt approvedSendReceipt = new TransferTransaction()
            .addApprovedNftTransfer(nft1, OPERATOR_ID, receiverAccountId)
            .setTransactionId(onBehalfOfTransactionId)
            .freezeWith(client)
            .sign(spenderKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Transfer serial 1 on behalf of the spender status:" + approvedSendReceipt.status);

        // Remove `spender's` allowance for serial 2
        TransactionReceipt deleteAllowanceReceipt = new AccountAllowanceDeleteTransaction()
            .deleteAllTokenNftAllowances(nft2, OPERATOR_ID)
            .execute(client)
            .getReceipt(client);
        System.out.println("Remove spender's allowance for serial 2 - status: " + deleteAllowanceReceipt.status);

        TransactionId onBehalfOfTransactionId2 = TransactionId.generate(spenderAccountId);

        // Sending NFT with serial number 2
        // Spender does not have an allowance to send serial 2, should end up with `SPENDER_DOES_NOT_HAVE_ALLOWANCE`
        try {
            new TransferTransaction()
                .addApprovedNftTransfer(nft2, OPERATOR_ID, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId2)
                .freezeWith(client)
                .sign(spenderKey)
                .execute(client)
                .getReceipt(client);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Example 2
        System.out.println("Example 2: Approve/delete allowances for ALL serial numbers at once");

        String[] CIDs2 = {
            "QmNPCiNA3Dsu3K5FxDPMG5Q3fZRwVTg14EXA92uqEeSRXn",
            "QmZ4dgAgt8owvnULxnKxNe8YqpavtVCXmc1Lt2XajFpJs9",
            "QmPzY5GxevjyfMUF5vEAjtyRoigzWp47MiKAtLBduLMC1T",
        };

        TransactionReceipt nftCreateReceipt2 = new TokenCreateTransaction()
            .setTokenName("NFT Token")
            .setTokenSymbol("NFTT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setDecimals(0)
            .setInitialSupply(0)
            .setMaxSupply(CIDs2.length)
            .setTreasuryAccountId(OPERATOR_ID)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(OPERATOR_KEY)
            .setSupplyKey(OPERATOR_KEY)
            .freezeWith(client)
            .execute(client)
            .getReceipt(client);

        TokenId nftTokenId2 = nftCreateReceipt2.tokenId;
        System.out.println("Created NFT with token id: " + nftTokenId2);

        List<TransactionReceipt> nftCollection2 = new ArrayList<>();
        for (int i = 0; i < CIDs2.length; i++) {
            nftCollection2.add(new TokenMintTransaction()
                .setTokenId(nftTokenId2)
                .setMetadata(List.of(CIDs2[i].getBytes(StandardCharsets.UTF_8)))
                .freezeWith(client)
                .execute(client)
                .getReceipt(client));

            System.out.println("Created NFT " + nftTokenId2 + " with serial: " + nftCollection2.get(i).serials.get(0));
        }

        // Create spender account
        PrivateKey delegatingSpenderKey2 = PrivateKey.generateECDSA();
        AccountId delegatingSpenderAccountId = new AccountCreateTransaction()
            .setKey(delegatingSpenderKey2)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("delegatingSpenderAccountId: " + delegatingSpenderAccountId);

        // Create receiver account
        PrivateKey receiverKey2 = PrivateKey.generateECDSA();
        AccountId receiverAccountId2 = new AccountCreateTransaction()
            .setKey(receiverKey2)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("spenderAccountId: " + receiverAccountId2);

        // Associate the spender with the NFT
        TransactionReceipt spenderAssociateReceipt2 = new TokenAssociateTransaction()
            .setAccountId(delegatingSpenderAccountId)
            .setTokenIds(List.of(nftTokenId2))
            .freezeWith(client)
            .sign(delegatingSpenderKey2)
            .execute(client)
            .getReceipt(client);
        System.out.println("Spender associate TX status: " + spenderAssociateReceipt2.status);

        // Associate the receiver with the NFT
        TransactionReceipt receiverAssociateReceipt2 = new TokenAssociateTransaction()
            .setAccountId(receiverAccountId2)
            .setTokenIds(List.of(nftTokenId2))
            .freezeWith(client)
            .sign(receiverKey2)
            .execute(client)
            .getReceipt(client);
        System.out.println("Receiver associate TX status: " + receiverAssociateReceipt2.status);

        NftId example2Nft1 = new NftId(nftTokenId2, 1);
        NftId example2Nft2 = new NftId(nftTokenId2, 2);
        NftId example2Nft3 = new NftId(nftTokenId2, 3);

        TransactionReceipt approveReceipt2 = new AccountAllowanceApproveTransaction()
            .approveTokenNftAllowanceAllSerials(nftTokenId2, OPERATOR_ID, delegatingSpenderAccountId)
            .execute(client)
            .getReceipt(client);
        System.out.println("Approve spender allowance for all serials - status: " + approveReceipt2.status);

        // Create delegate spender account
        PrivateKey spenderKey2 = PrivateKey.generateECDSA();
        AccountId spenderAccountId2 = new AccountCreateTransaction()
            .setKey(spenderKey2)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("spenderAccountId2: " + spenderAccountId2);

        // Give delegatingSpender allowance for NFT with serial number 3 on behalf of spender account which has `approveForAll` rights
        TransactionReceipt approveDelegateAllowanceReceipt = new AccountAllowanceApproveTransaction()
            .approveTokenNftAllowance(example2Nft3, OPERATOR_ID, spenderAccountId2, delegatingSpenderAccountId)
            .freezeWith(client)
            .sign(delegatingSpenderKey2)
            .execute(client)
            .getReceipt(client);
        System.out.println("Approve delegated spender allowance for serial 3 - status: " + approveDelegateAllowanceReceipt.status);

        // Generate TransactionId from spender's account id in order
        // for the transaction to be executed on behalf of the spender
        TransactionId delegatedOnBehalfOfTxId = TransactionId.generate(spenderAccountId2);

        // Sending NFT with serial number 1
        // `Delegated spender` has an allowance to send serial 3, should end up with `SUCCESS`
        TransactionReceipt delegatedSendTx = new TransferTransaction()
            .addApprovedNftTransfer(example2Nft3, OPERATOR_ID, receiverAccountId2)
            .setTransactionId(delegatedOnBehalfOfTxId)
            .freezeWith(client)
            .sign(spenderKey2)
            .execute(client)
            .getReceipt(client);
        System.out.println("Transfer serial 3 on behalf of the delegated spender status:" + delegatedSendTx.status);

        // Generate TransactionId from spender's account id in order
        // for the transaction to be executed on behalf of the spender
        TransactionId onBehalfOfTransactionId3 = TransactionId.generate(delegatingSpenderAccountId);

        // Sending NFT with serial number 1
        // `Spender` has an allowance to send serial 1, should end up with `SUCCESS`
        TransactionReceipt approvedSendReceipt3 = new TransferTransaction()
            .addApprovedNftTransfer(example2Nft1, OPERATOR_ID, receiverAccountId2)
            .setTransactionId(onBehalfOfTransactionId3)
            .freezeWith(client)
            .sign(delegatingSpenderKey2)
            .execute(client)
            .getReceipt(client);
        System.out.println("Transfer serial 1 on behalf of the spender status:" + approvedSendReceipt3.status);

        // Remove spender's allowance for ALL serials
        TransactionReceipt deleteAllowanceReceipt2 = new AccountAllowanceApproveTransaction()
            .deleteTokenNftAllowanceAllSerials(nftTokenId2, OPERATOR_ID, delegatingSpenderAccountId)
            .execute(client)
            .getReceipt(client);
        System.out.println("Remove spender's allowance for serial 2 - status: " + deleteAllowanceReceipt2.status);

        // Generate TransactionId from spender's account id in order
        // for the transaction to be executed on behalf of the spender
        TransactionId onBehalfOfTransactionId4 = TransactionId.generate(delegatingSpenderAccountId);

        // Sending NFT with serial number 2
        // `Spender` does not have an allowance to send serial 2, should end up with `SPENDER_DOES_NOT_HAVE_ALLOWANCE`
        try {
            new TransferTransaction()
                .addApprovedNftTransfer(example2Nft2, OPERATOR_ID, receiverAccountId2)
                .setTransactionId(onBehalfOfTransactionId4)
                .freezeWith(client)
                .sign(delegatingSpenderKey2)
                .execute(client)
                .getReceipt(client);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
