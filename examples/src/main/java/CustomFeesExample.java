/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2023 Hedera Hashgraph, LLC
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
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.CustomFee;
import com.hedera.hashgraph.sdk.CustomFixedFee;
import com.hedera.hashgraph.sdk.CustomFractionalFee;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TokenDissociateTransaction;
import com.hedera.hashgraph.sdk.TokenFeeScheduleUpdateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenInfo;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenUpdateTransaction;
import com.hedera.hashgraph.sdk.TokenWipeTransaction;
import com.hedera.hashgraph.sdk.TransactionRecord;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class CustomFeesExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private CustomFeesExample() {
    }

    public static void main(String[] args)
        throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Create three accounts, Alice, Bob, and Charlie.  Alice will be the treasury for our example token.
        // Fees only apply in transactions not involving the treasury, so we need two other accounts.

        PrivateKey aliceKey = PrivateKey.generateED25519();
        AccountId aliceId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(aliceKey)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey bobKey = PrivateKey.generateED25519();
        AccountId bobId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(bobKey)
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey charlieKey = PrivateKey.generateED25519();
        AccountId charlieId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(charlieKey)
            .freezeWith(client)
            .sign(charlieKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        System.out.println("Alice: " + aliceId);
        System.out.println("Bob: " + bobId);
        System.out.println("Charlie: " + charlieId);

        // Let's start with a custom fee list of 1 fixed fee.  A custom fee list can be a list of up to
        // 10 custom fees, where each fee is a fixed fee or a fractional fee.
        // This fixed fee will mean that every time Bob transfers any number of tokens to Charlie,
        // Alice will collect 1 Hbar from each account involved in the transaction who is SENDING
        // the Token (in this case, Bob).

        CustomFixedFee customHbarFee = new CustomFixedFee()
            .setHbarAmount(new Hbar(1))
            .setFeeCollectorAccountId(aliceId);
        List<CustomFee> hbarFeeList = Collections.singletonList(customHbarFee);

        // In this example the fee is in Hbar, but you can charge a fixed fee in a token if you'd like.
        // EG, you can make it so that each time an account transfers Foo tokens,
        // they must pay a fee in Bar tokens to the fee collecting account.
        // To charge a fixed fee in tokens, instead of calling setHbarAmount(), call
        // setDenominatingTokenId(tokenForFee) and setAmount(tokenFeeAmount).

        // Setting the feeScheduleKey to Alice's key will enable Alice to change the custom
        // fees list on this token later using the TokenFeeScheduleUpdateTransaction.
        // We will create an initial supply of 100 of these tokens.

        TokenId tokenId = new TokenCreateTransaction()
            .setTokenName("Example Token")
            .setTokenSymbol("EX")
            .setAdminKey(aliceKey)
            .setSupplyKey(aliceKey)
            .setFeeScheduleKey(aliceKey)
            .setWipeKey(aliceKey)
            .setTreasuryAccountId(aliceId)
            .setCustomFees(hbarFeeList)
            .setInitialSupply(100)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client)
            .tokenId;

        System.out.println("Token: " + tokenId);

        TokenInfo tokenInfo1 = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Custom Fees according to TokenInfoQuery:");
        System.out.println(tokenInfo1.customFees);

        // We must associate the token with Bob and Charlie before they can trade in it.

        new TokenAssociateTransaction()
            .setAccountId(bobId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getReceipt(client);

        new TokenAssociateTransaction()
            .setAccountId(charlieId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(charlieKey)
            .execute(client)
            .getReceipt(client);

        // give all 100 tokens to Bob
        new TransferTransaction()
            .addTokenTransfer(tokenId, bobId, 100)
            .addTokenTransfer(tokenId, aliceId, -100)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        Hbar aliceHbar1 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .hbars;
        System.out.println("Alice's Hbar balance before Bob transfers 20 tokens to Charlie: " + aliceHbar1);

        TransactionRecord record1 = new TransferTransaction()
            .addTokenTransfer(tokenId, bobId, -20)
            .addTokenTransfer(tokenId, charlieId, 20)
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getRecord(client);

        Hbar aliceHbar2 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .hbars;
        System.out.println("Alices's Hbar balance after Bob transfers 20 tokens to Charlie: " + aliceHbar2);

        System.out.println("Assessed fees according to transaction record:");
        System.out.println(record1.assessedCustomFees);

        // Let's use the TokenUpdateFeeScheduleTransaction with Alice's key to change the custom fees on our token.
        // TokenUpdateFeeScheduleTransaction will replace the list of fees that apply to the token with
        // an entirely new list.  Let's charge a 10% fractional fee.  This means that when Bob attempts to transfer
        // 20 tokens to Charlie, 10% of the tokens he attempts to transfer (2 in this case) will be transferred to
        // Alice instead.

        // Fractional fees default to FeeAssessmentMethod.INCLUSIVE, which is the behavior described above.
        // If you set the assessment method to EXCLUSIVE, then when Bob attempts to transfer 20 tokens to Charlie,
        // Charlie will receive all 20 tokens, and Bob will be charged an _additional_ 10% fee which
        // will be transferred to Alice.

        CustomFractionalFee customFractionalFee = new CustomFractionalFee()
            .setNumerator(1)
            .setDenominator(10)
            .setMin(1)
            .setMax(10)
            // .setAssessmentMethod(FeeAssessmentMethod.EXCLUSIVE)
            .setFeeCollectorAccountId(aliceId);
        List<CustomFee> fractionalFeeList = Collections.singletonList(customFractionalFee);

        new TokenFeeScheduleUpdateTransaction()
            .setTokenId(tokenId)
            .setCustomFees(fractionalFeeList)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        TokenInfo tokenInfo2 = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Custom Fees according to TokenInfoQuery:");
        System.out.println(tokenInfo2.customFees);

        Map<TokenId, Long> aliceTokens3 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;
        System.out.println("Alice's token balance before Bob transfers 20 tokens to Charlie: " + aliceTokens3);

        TransactionRecord record2 = new TransferTransaction()
            .addTokenTransfer(tokenId, bobId, -20)
            .addTokenTransfer(tokenId, charlieId, 20)
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getRecord(client);

        Map<TokenId, Long> aliceTokens4 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;
        System.out.println("Alices's token balance after Bob transfers 20 tokens to Charlie: " + aliceTokens4);

        System.out.println("Token transfers according to transaction record:");
        System.out.println(record2.tokenTransfers);
        System.out.println("Assessed fees according to transaction record:");
        System.out.println(record2.assessedCustomFees);

        // clean up

        // move token to operator account
        new TokenAssociateTransaction()
            .setAccountId(client.getOperatorAccountId())
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setAdminKey(OPERATOR_KEY)
            .setSupplyKey(OPERATOR_KEY)
            .setFeeScheduleKey(OPERATOR_KEY)
            .setWipeKey(OPERATOR_KEY)
            .setTreasuryAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        // wipe token on created accounts
        Map<TokenId, Long> charlieTokensBeforeWipe = new AccountBalanceQuery()
            .setAccountId(charlieId)
            .execute(client)
            .tokens;
        System.out.println("Charlie's token balance (before wipe): " + charlieTokensBeforeWipe.get(tokenId));

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAmount(charlieTokensBeforeWipe.get(tokenId))
            .setAccountId(charlieId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        Map<TokenId, Long> bobTokensBeforeWipe = new AccountBalanceQuery()
            .setAccountId(bobId)
            .execute(client)
            .tokens;
        System.out.println("Bob's token balance (before wipe): " + bobTokensBeforeWipe.get(tokenId));

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAmount(bobTokensBeforeWipe.get(tokenId))
            .setAccountId(bobId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        Map<TokenId, Long> aliceTokensBeforeWipe = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;
        System.out.println("Alice's token balance (before wipe): " + aliceTokensBeforeWipe.get(tokenId));

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAmount(aliceTokensBeforeWipe.get(tokenId))
            .setAccountId(aliceId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        // delete accounts and token
        new AccountDeleteTransaction()
            .setAccountId(charlieId)
            .setTransferAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(charlieKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(bobId)
            .setTransferAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(aliceId)
            .setTransferAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        new TokenDeleteTransaction()
            .setTokenId(tokenId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        client.close();
    }
}
