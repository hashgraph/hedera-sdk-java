package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.file.FileId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {
    private static final AccountId nodeAcctId = new AccountId(0);
    private static final AccountId acctId = new AccountId(3);
    private static final Ed25519PrivateKey key1 = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");


    private static final Instant txnStartAt = Instant.parse("2019-04-18T20:50:00Z");
    private static final TransactionId txnId = new TransactionId(acctId, txnStartAt);

    // a different instance for each test
    private final Transaction txn = new FileDeleteTransaction(null)
        .setTransactionId(txnId)
        .setNodeAccountId(nodeAcctId)
        .setFileId(new FileId(0, 0, 0))
        .build();

    @Test
    @DisplayName("validate() requires a signature")
    void validateRequiresSignature() {
        assertThrows(
            IllegalStateException.class,
            txn::validate,
            "Transaction failed validation:\n"
                + "Transaction requires at least one signature"
        );
    }

    @Test
    @DisplayName("validate() accepts a single signature")
    void validateOneSignature() {
        assertDoesNotThrow(() -> txn.sign(key1).validate());
    }

    @Test
    @DisplayName("validate() accepts two signatures")
    void validateTwoSignatures() {
        final var key2 = Ed25519PrivateKey.generate();

        assertDoesNotThrow(
            () -> txn.sign(key1).sign(key2).validate()
        );
    }

    @Test
    @DisplayName("validate() forbids duplicate signing keys")
    void validateForbidsDuplicates() {
        assertThrows(
            IllegalArgumentException.class,
            () -> txn.sign(key1).sign(key1),
            "transaction already signed with key: " + key1
        );
    }

    @Test
    @DisplayName("transaction goes to bytes and back")
    void testSerialization() throws InvalidProtocolBufferException {
        final var txn1Bytes = txn.sign(key1).toBytes();

        final var txn2 = Transaction.fromBytes(txn1Bytes);

        assertEquals(txn.inner.build(), txn2.inner.build());
        assertEquals(txn.nodeAccountId, txn2.nodeAccountId);
        assertEquals(txn.transactionId, txn2.transactionId);
    }
}
