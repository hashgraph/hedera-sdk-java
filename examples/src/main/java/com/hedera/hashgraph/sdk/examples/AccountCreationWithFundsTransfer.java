package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.concurrent.TimeoutException;

public class AccountCreationWithFundsTransfer {

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("OPERATOR_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("OPERATOR_KEY"));

        Client client = Client.forTestnet();

        client
            .setOperator(myAccountId, myPrivateKey)
            .setDefaultMaxTransactionFee(new Hbar(100))
            .setDefaultMaxQueryPayment(new Hbar(50));

        PrivateKey newAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey newAccountPublicKey = newAccountPrivateKey.getPublicKey();

        TransactionResponse newAccount = new AccountCreateTransaction()
            .setKey(newAccountPublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        AccountId accountId = newAccount.getReceipt(client).accountId;

        System.out.println(accountId);

        AccountBalance accountBalance = new AccountBalanceQuery()
            .setAccountId(accountId)
            .execute(client);

        System.out.println("AccountBalance: " + accountBalance);

        TransactionResponse sendHbar = new TransferTransaction()
            .addHbarTransfer(myAccountId, Hbar.fromTinybars(-100000000)) //Sending account
            .addHbarTransfer(accountId, Hbar.fromTinybars(100000000)) //Receiving account
            .execute(client);

        System.out.println("The transfer transaction was: " +sendHbar.getReceipt(client).status);

        Hbar queryCost = new AccountBalanceQuery()
            .setAccountId(accountId)
            .getCost(client);

        System.out.println("The cost of this query is: " + queryCost);


        //Check the new account's balance
        AccountBalance accountBalanceNew = new AccountBalanceQuery()
            .setAccountId(accountId)
            .execute(client);

        System.out.println("The account balance after the transfer: " +accountBalanceNew.hbars);
    }
}
