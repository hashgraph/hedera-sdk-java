package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Collections;

public class PrivateKeyTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @Test
    void signTransactionWorks() throws InvalidProtocolBufferException {
       byte[] bytes = new AccountCreateTransaction()
            .setNodeAccountIds(Collections.singletonList(AccountId.fromString("0.0.5005")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setKey(unusedPrivateKey)
            .setInitialBalance(Hbar.fromTinybars(450))
            .setProxyAccountId(AccountId.fromString("0.0.1001"))
            .setReceiverSignatureRequired(true)
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
           .toBytes();

       AccountCreateTransaction transaction = (AccountCreateTransaction) Transaction.fromBytes(bytes);
       unusedPrivateKey.signTransaction(transaction);
    }
}
