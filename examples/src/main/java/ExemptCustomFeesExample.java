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

import java.util.Objects;
import java.util.concurrent.TimeoutException;

/*
    Example for HIP-573: Blanket exemptions for custom fee collectors
    1. Create accounts A, B, and C
    2. Create a fungible token that has three fractional fees
    Fee #1 sends 1/100 of the transferred value to collector 0.0.A.
    Fee #2 sends 2/100 of the transferred value to collector 0.0.B.
    Fee #3 sends 3/100 of the transferred value to collector 0.0.C.
    3. Collector 0.0.B sends 10_000 units of the token to 0.0.A.
    4. Get the transaction fee for that transfer transaction
    5. Show that the fee collector accounts in the custom fee list of the token
    that was created was not charged a custom fee in the transfer
*/
public final class ExemptCustomFeesExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private ExemptCustomFeesExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException {
        Client client = Client.forName(HEDERA_NETWORK);
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Step 1
         * Create accounts A, B, and C
         */

        PrivateKey firstAccountPrivateKey = PrivateKey.generateED25519();
        AccountId firstAccountId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(firstAccountPrivateKey)
            .freezeWith(client)
            .sign(firstAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey secondAccountPrivateKey = PrivateKey.generateED25519();
        AccountId secondAccountId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(secondAccountPrivateKey)
            .freezeWith(client)
            .sign(secondAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey thirdAccountPrivateKey = PrivateKey.generateED25519();
        AccountId thirdAccountId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(thirdAccountPrivateKey)
            .freezeWith(client)
            .sign(thirdAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        /*
         * Step 2
         * 2. Create a fungible token that has three fractional fees
         * Fee #1 sends 1/100 of the transferred value to collector 0.0.A.
         * Fee #2 sends 2/100 of the transferred value to collector 0.0.B.
         * Fee #3 sends 3/100 of the transferred value to collector 0.0.C.
         */

        CustomFractionalFee fee1 = new CustomFractionalFee()
            .setFeeCollectorAccountId(firstAccountId)
            .setNumerator(1)
            .setDenominator(100)
            .setAllCollectorsAreExempt(true);

        CustomFractionalFee fee2 = new CustomFractionalFee()
            .setFeeCollectorAccountId(secondAccountId)
            .setNumerator(2)
            .setDenominator(100)
            .setAllCollectorsAreExempt(true);

        CustomFractionalFee fee3 = new CustomFractionalFee()
            .setFeeCollectorAccountId(thirdAccountId)
            .setNumerator(3)
            .setDenominator(100)
            .setAllCollectorsAreExempt(true);

        TokenCreateTransaction tokenCreateTransaction = new TokenCreateTransaction()
            .setTokenName("HIP-573 Token")
            .setTokenSymbol("H573")
            .setTokenType(TokenType.FUNGIBLE_COMMON)
            .setTreasuryAccountId(OPERATOR_ID)
            .setAutoRenewAccountId(OPERATOR_ID)
            .setAdminKey(OPERATOR_KEY)
            .setFreezeKey(OPERATOR_KEY)
            .setWipeKey(OPERATOR_KEY)
            .setInitialSupply(100_000_000)
            .setDecimals(2)
            .setCustomFees(List.of(fee1, fee2, fee3))
            .freezeWith(client)
            .sign(firstAccountPrivateKey)
            .sign(secondAccountPrivateKey)
            .sign(thirdAccountPrivateKey);

        TokenId tokenId = tokenCreateTransaction
            .execute(client)
            .getReceipt(client)
            .tokenId;
        System.out.println("TokenId: " + tokenId);

        /*
         * Step 3
         * Collector 0.0.B sends 10_000 units of the token to 0.0.A.
         */

        // Send 10_000 units from the operator to the second account
        new TransferTransaction()
            .addTokenTransfer(tokenId, OPERATOR_ID, -10_000)
            .addTokenTransfer(tokenId, secondAccountId, 10_000)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client);

        TransactionResponse tokenTransferResponse = new TransferTransaction()
            .addTokenTransfer(tokenId, secondAccountId, -10_000)
            .addTokenTransfer(tokenId, firstAccountId, 10_000)
            .freezeWith(client)
            .sign(secondAccountPrivateKey)
            .execute(client);


        /*
         * Step 4
         * Get the transaction fee for that transfer transaction
         */

        Hbar transactionFee = tokenTransferResponse
            .getRecord(client)
            .transactionFee;
        System.out.println("Txfee: " + transactionFee);

        /*
         * Step 5
         * Show that the fee collector accounts in the custom fee list
         * of the token that was created was not charged a custom fee in the transfer
         */

        Long firstAccountBalanceAfter = new AccountBalanceQuery()
            .setAccountId(firstAccountId)
            .execute(client)
            .tokens.get(tokenId);

        Long secondAccountBalanceAfter = new AccountBalanceQuery()
            .setAccountId(secondAccountId)
            .execute(client)
            .tokens.get(tokenId);

        Long thirdAccountBalanceAfter = new AccountBalanceQuery()
            .setAccountId(thirdAccountId)
            .execute(client)
            .tokens.get(tokenId);

        System.out.println("First account balance after TransferTransaction: " + firstAccountBalanceAfter);
        System.out.println("Second account balance after TransferTransaction: " + secondAccountBalanceAfter);
        System.out.println("Third account balance after TransferTransaction: " + thirdAccountBalanceAfter);
    }
}
