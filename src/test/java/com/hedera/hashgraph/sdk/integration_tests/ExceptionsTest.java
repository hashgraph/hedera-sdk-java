package com.hedera.hashgraph.sdk.integration_tests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPrecheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.HederaRecordStatusException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractFunctionParams;
import com.hedera.hashgraph.sdk.contract.ContractFunctionResult;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        HederaPrecheckStatusException exception = assertThrows(
            HederaPrecheckStatusException.class, () -> badTxn.execute(testEnv.client));

        assertEquals("transaction "
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

        HederaReceiptStatusException exception = assertThrows(
            HederaReceiptStatusException.class,
            () -> {
                badTxn
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

        assertEquals("receipt for transaction "
                + badTxn.id + " contained error status INVALID_SIGNATURE",
            exception.getMessage());
    }

    @Test
    @DisplayName("HederaRecordStatusException is thrown on contract construction failure")
    void recordException() throws IOException, HederaStatusException {
        ClassLoader cl = getClass().getClassLoader();

        Gson gson = new Gson();

        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("fail-constructor.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get fail-constructor.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
        }

        String byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();

        Ed25519PrivateKey privateKey = Ed25519PrivateKey.generate();

        FileId bytecodeFile = new FileCreateTransaction()
            .setContents(byteCodeHex)
            // we don't care about editing this file later
            .addKey(privateKey.publicKey)
            .setMaxTransactionFee(new Hbar(20))
            .build(testEnv.client)
            .sign(privateKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .getFileId();

        TransactionId badTxnId = new ContractCreateTransaction()
            .setMaxTransactionFee(new Hbar(20))
            .setBytecodeFileId(bytecodeFile)
            .setGas(1000)
            .setConstructorParams(new ContractFunctionParams().addString("error!"))
            .execute(testEnv.client);

        HederaRecordStatusException e = assertThrows(
            HederaRecordStatusException.class,
            () -> badTxnId.getRecord(testEnv.client));

        assertEquals(e.status, Status.ContractRevertExecuted);

        ContractFunctionResult createResult = e.record.getContractCreateResult();
        assertEquals(createResult.errorMessage, "REVERT opcode executed");
        assertEquals(createResult.getString(0), "error!");
    }
}
