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

public final class ZeroTokenOperationsExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private ZeroTokenOperationsExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        client.setDefaultMaxTransactionFee(new Hbar(10));

        PrivateKey alicePrivateKey = PrivateKey.generateED25519();
        PublicKey alicePublicKey = alicePrivateKey.getPublicKey();

        AccountCreateTransaction transaction = new AccountCreateTransaction()
            .setKey(alicePublicKey)
            .setInitialBalance(Hbar.from(10))
            .freezeWith(client);

        transaction = transaction.signWithOperator(client);

        TransactionResponse response = transaction.execute(client);
        AccountId aliceAccountId = response.getReceipt(client).accountId;

        ContractHelper contractHelper = new ContractHelper(
            "precompile-example/ZeroTokenOperations.json",
            new ContractFunctionParameters()
                .addAddress(OPERATOR_ID.toSolidityAddress())
                .addAddress(aliceAccountId.toSolidityAddress()),
            client
        );

        // Configure steps in ContractHelper
        contractHelper
            .setPayableAmountForStep(0, Hbar.from(20))
            .addSignerForStep(1, alicePrivateKey);

        // step 0 creates a fungible token
        // step 1 Associate with account
        // step 2 transfer the token by passing a zero value
        // step 3 mint the token by passing a zero value
        // step 4 burn the token by passing a zero value
        // step 5 wipe the token by passing a zero value

        contractHelper.executeSteps(/* from step */ 0, /* to step */ 5, client);

        // step 6 use SDK and transfer passing a zero value
        // Create Fungible Token
        System.out.println("Attempting to execute step 6");

        TokenCreateTransaction tokenCreateTransaction = new TokenCreateTransaction()
            .setTokenName("Black Sea LimeChain Token")
            .setTokenSymbol("BSL")
            .setTreasuryAccountId(OPERATOR_ID)
            .setInitialSupply(10000) // Total supply = 10000 / 10 ^ 2
            .setDecimals(2)
            .setAutoRenewAccountId(OPERATOR_ID)
            .freezeWith(client);

        tokenCreateTransaction = tokenCreateTransaction.signWithOperator(client);
        TransactionResponse responseTokenCreate = tokenCreateTransaction.execute(client);

        TokenId tokenId = responseTokenCreate.getReceipt(client).tokenId;

        // Associate Token with Account.
        // Accounts on hedera have to opt in to receive any types of token that aren't HBAR.
        TokenAssociateTransaction tokenAssociateTransaction = new TokenAssociateTransaction()
            .setAccountId(aliceAccountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client);

        TokenAssociateTransaction signedTxForAssociateToken = tokenAssociateTransaction.sign(alicePrivateKey);
        TransactionResponse txResponseAssociatedToken = signedTxForAssociateToken.execute(client);

        Status status = txResponseAssociatedToken.getReceipt(client).status;

        //Transfer token
        TransferTransaction transferToken = new TransferTransaction()
            .addTokenTransfer(tokenId, OPERATOR_ID, 0) // deduct 0 tokens
            .addTokenTransfer(tokenId, aliceAccountId, 0) // increase balance by 0
            .freezeWith(client);

        TransferTransaction signedTransferTokenTX = transferToken.signWithOperator(client);
        TransactionResponse txResponseTransferToken = signedTransferTokenTX.execute(client);

        //Verify the transaction reached consensus
        TransactionRecord transferReceiptRecord = txResponseTransferToken.getRecord(client);

        System.out.println(
            "step 6 completed, and returned valid result. TransactionId: " + transferReceiptRecord.transactionId);

        System.out.println("All steps completed with valid results.");
    }
}
