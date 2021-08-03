import com.hedera.hashgraph.sdk.*;
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

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Create three accounts, Alice, Bob, and Charlie.  Alice will be the treasury for our example token.
        // Fees only apply in transactions not involving the treasury, so we need two other accounts.

        PrivateKey aliceKey = PrivateKey.generate();
        AccountId aliceId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(50))
            .setKey(aliceKey)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey bobKey = PrivateKey.generate();
        AccountId bobId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(50))
            .setKey(bobKey)
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey charlieKey = PrivateKey.generate();
        AccountId charlieId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(50))
            .setKey(charlieKey)
            .freezeWith(client)
            .sign(charlieKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        // Let's start with a custom fee list of 1 fixed fee.  A custom fee list can be a list of up to
        // 10 custom fees, where each fee is a fixed fee or a fractional fee.
        // This fixed fee will mean that every time Bob transfers any number of tokens to Charlie,
        // Alice will collect 1 Hbar from each account involved in the transaction who is SENDING
        // the Token (in this case, Bob).

        /*
        CustomFixedFee customHbarFee = new CustomFixedFee()
            .setHbarAmount(new Hbar(1))
            .setFeeCollectorAccountId(aliceId);
        List<CustomFee> hbarFeeList = Collections.singletonList(customHbarFee);
         */

        // Setting the feeScheduleKey to Alice's key will enable Alice to change the custom
        // fees list on this token later using the TokenFeeScheduleUpdateTransaction.
        // We will create an initial supply of 100 of these tokens.

        // TEMP
        CustomFixedFee customTokenFee = new CustomFixedFee()
            .setDemoninatingTokenToSameToken()
            .setAmount(1)
            .setFeeCollectorAccountId(aliceId);
        List<CustomFee> tokenFeeList = Collections.singletonList(customTokenFee);

        TokenId tokenId = new TokenCreateTransaction()
            .setTokenName("Example Token")
            .setTokenSymbol("EX")
            .setAdminKey(aliceKey)
            .setSupplyKey(aliceKey)
            .setFeeScheduleKey(aliceKey)
            .setTreasuryAccountId(aliceId)
            //.setCustomFees(hbarFeeList)
            .setCustomFees(tokenFeeList) // TEMP
            .setInitialSupply(100)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client)
            .tokenId;

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

        /*

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
        // an entirely new list.  Instead of charging the fee in Hbar, let's charge a fee in tokens.
        // While not demonstrated here, you can also use setDenominatingTokenId() if you wish to charge a fee
        // in a different token.  EG, you can make it so that each time an account transfers Foo tokens,
        // they must pay a fee in Bar tokens to the fee collecting account.

        CustomFixedFee customTokenFee = new CustomFixedFee()
            .setDemoninatingTokenToSameToken()
            .setAmount(1)
            .setFeeCollectorAccountId(aliceId);
        List<CustomFee> tokenFeeList = Collections.singletonList(customTokenFee);

        new TokenFeeScheduleUpdateTransaction()
            .setTokenId(tokenId)
            .setCustomFees(tokenFeeList)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        TokenInfo tokenInfo2 = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Custom Fees according to TokenInfoQuery:");
        System.out.println(tokenInfo2.customFees);

         */

        Map<TokenId, Long> aliceTokens1 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;
        System.out.println("Alice's token balance before Bob transfers 20 tokens to Charlie: " + aliceTokens1);

        TransactionRecord record2 = new TransferTransaction()
            .addTokenTransfer(tokenId, bobId, -20)
            .addTokenTransfer(tokenId, charlieId, 20)
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getRecord(client);

        Map<TokenId, Long> aliceTokens2 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;
        System.out.println("Alices's token balance after Bob transfers 20 tokens to Charlie: " + aliceTokens2);

        System.out.println("Assessed fees according to transaction record:");
        System.out.println(record2.assessedCustomFees);

        // Now a 10% fractional fee.
        /*

        CustomFractionalFee customFractionalFee = new CustomFractionalFee()
            .setNumerator(1)
            .setDenominator(10)
            .setMin(1)
            .setMax(10)
            .setFeeCollectorAccountId(aliceId);
        List<CustomFee> fractionalFeeList = Collections.singletonList(customFractionalFee);

        new TokenFeeScheduleUpdateTransaction()
            .setTokenId(tokenId)
            .setCustomFees(fractionalFeeList)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        TokenInfo tokenInfo3 = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Custom Fees according to TokenInfoQuery:");
        System.out.println(tokenInfo3.customFees);

        Map<TokenId, Long> aliceTokens3 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;
        System.out.println("Alice's token balance before Bob transfers 20 tokens to Charlie: " + aliceTokens3);

        TransactionRecord record3 = new TransferTransaction()
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

        System.out.println("Assessed fees according to transaction record:");
        System.out.println(record3.assessedCustomFees);
         */

        // clean up

        new TokenDeleteTransaction()
            .setTokenId(tokenId)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

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

        client.close();
    }
}
