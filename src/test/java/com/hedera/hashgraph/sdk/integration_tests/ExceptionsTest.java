package com.hedera.hashgraph.sdk.integration_tests;

import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPrecheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ExceptionsTest {

    private final TestEnv testEnv = new TestEnv();

    @Test
    @DisplayName("HederaPrecheckStatusException is thrown for bad account amounts")
    void precheckException() {
        Transaction badTxn = new CryptoTransferTransaction()
            .addSender(new AccountId(2), new Hbar(5))
            .addSender(new AccountId(2), new Hbar(5))
            .addRecipient(testEnv.operatorId, new Hbar(10))
            .build(testEnv.client);

        HederaPrecheckStatusException exception = Assertions.assertThrows(
            HederaPrecheckStatusException.class, () -> badTxn.execute(testEnv.client));

        Assertions.assertEquals("transaction "
                + badTxn.id + " failed precheck with status ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS",
            exception.getMessage());
    }

    @Test
    @DisplayName("HederaReceiptStatusException is thrown for bad signature")
    void receiptException() {
        Transaction badTxn = new CryptoTransferTransaction()
            .addSender(new AccountId(2), new Hbar(10))
            .addRecipient(testEnv.operatorId, new Hbar(10))
            .build(testEnv.client);

        HederaReceiptStatusException exception = Assertions.assertThrows(
            HederaReceiptStatusException.class,
            () -> {
                badTxn
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

        Assertions.assertEquals("receipt for transaction "
                + badTxn.id + " contained error status INVALID_SIGNATURE",
            exception.getMessage());
    }
}
