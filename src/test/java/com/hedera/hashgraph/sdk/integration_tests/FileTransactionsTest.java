package com.hedera.hashgraph.sdk.integration_tests;

import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPrecheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileAppendTransaction;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.file.FileInfo;
import com.hedera.hashgraph.sdk.file.FileInfoQuery;
import com.hedera.hashgraph.sdk.file.FileUpdateTransaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileTransactionsTest {
    private final TestEnv testEnv = new TestEnv();

    {
        testEnv.client.setMaxTransactionFee(new Hbar(100));
    }

    @Test
    void testFileMultiKey() throws HederaStatusException {
        Ed25519PrivateKey key1 = Ed25519PrivateKey.generate();
        Ed25519PrivateKey key2 = Ed25519PrivateKey.generate();

        // CREATE FILE
        final FileId fileId = new FileCreateTransaction()
            .addKey(key1.publicKey)
            .addKey(key2.publicKey)
            .build(testEnv.client)
            .sign(key1)
            .sign(key2)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .getFileId();

        // ASSERT FILE INFO
        FileInfo fileInfo1 = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(fileInfo1.size, 0);
        assertFalse(fileInfo1.isDeleted);
        assertTrue(fileInfo1.expirationTime.isAfter(Instant.now()));
        assertIterableEquals(fileInfo1.keys, Arrays.asList(key1.publicKey, key2.publicKey));

        // ADD KEY3
        Ed25519PrivateKey key3 = Ed25519PrivateKey.generate();

        TransactionReceipt receipt = new FileUpdateTransaction()
            .setFileId(fileId)
            // you hae to add all keys if you just want to add a new key
            .addKey(key1.publicKey)
            .addKey(key2.publicKey)
            .addKey(key3.publicKey)
            .build(testEnv.client)
            .sign(key1)
            .sign(key2)
            .sign(key3)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertEquals(Status.Success, receipt.status);

        // ASSERT KEY3 HAS BEEN ADDED
        FileInfo fileInfo2 = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertIterableEquals(fileInfo2.keys, Arrays.asList(key1.publicKey, key2.publicKey, key3.publicKey));

        // REQUIRE ALL KEYS TO SIGN UPDATE
        assertThrows(HederaReceiptStatusException.class, () ->
            new FileUpdateTransaction()
                .setFileId(fileId)
                .setContents("Hello, world!")
                .build(testEnv.client)
                .sign(key1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client));

        // ASSERT UPDATE FAILED
        Assertions.assertArrayEquals(
            new byte[] {},
            new FileContentsQuery().setFileId(fileId).execute(testEnv.client));

        // TEST ALL KEYS SIGNING APPEND
        new FileAppendTransaction()
            .setFileId(fileId)
            .setContents("Hello, world!")
            .build(testEnv.client)
            .sign(key1)
            .sign(key2)
            .sign(key3)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        Assertions.assertArrayEquals(
            "Hello, world!".getBytes(StandardCharsets.UTF_8),
            new FileContentsQuery().setFileId(fileId).execute(testEnv.client));

        // TEST ONE KEY SIGNING DELETE
        new FileDeleteTransaction()
            .setFileId(fileId)
            .build(testEnv.client)
            .sign(key1)
            .sign(key2)
            .sign(key3)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        HederaPrecheckStatusException exception = assertThrows(
            HederaPrecheckStatusException.class,
            () -> new FileInfoQuery()
                .setFileId(fileId)
                .execute(testEnv.client));

        assertEquals(exception.status, Status.FileDeleted);
    }
}
