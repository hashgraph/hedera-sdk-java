package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetTime;
import java.time.ZoneOffset;

class FreezeTransactionTest {

    @Test
    @DisplayName("correct transactions should build")
    void correctTxn() {
        final Instant now = Instant.ofEpochSecond(1554158542);
        final TransactionId txnId = TransactionId.withValidStart(new AccountId(2), now);
        final AccountId nodeAcctId = new AccountId(3);

        new FreezeTransaction()
            .setTransactionId(txnId)
            .setNodeAccountId(nodeAcctId)
            // start and end times being 0:00 is technically correct
            .build();

            new FreezeTransaction()
                .setTransactionId(txnId)
                .setNodeAccountId(nodeAcctId)
                .setStartTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC))
                .setEndTime(OffsetTime.of(23, 59, 0, 0, ZoneOffset.UTC))
                .build();

        new FreezeTransaction()
            .setTransactionId(txnId)
            .setNodeAccountId(nodeAcctId)
            .setStartTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC))
            .setEndTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC))
            .build();

        new FreezeTransaction()
            .setTransactionId(txnId)
            .setNodeAccountId(nodeAcctId)
            .setStartTime(OffsetTime.of(23, 59, 0, 0, ZoneOffset.UTC))
            .setEndTime(OffsetTime.of(23, 59, 0, 0, ZoneOffset.UTC))
            .build();
    }

    @Test
    @DisplayName("transaction should serialize properly")
    void serializeTest() {
        final Instant now = Instant.ofEpochSecond(1554158542);
        final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        Transaction txn = new FreezeTransaction()
            .setTransactionId(TransactionId.withValidStart(new AccountId(2), now))
            .setNodeAccountId(new AccountId(3))
            .setStartTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC))
            .setEndTime(OffsetTime.of(23, 59, 0, 0, ZoneOffset.UTC))
            .setMaxTransactionFee(100_000_000)
            .build()
            .sign(key);

        Assertions.assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"?\\237\\317\\a>P\\245\\305\\201\\026\\3553\\365O[\\312\\344\\23781\\207*2/\\033H\\376R\\314\\334\\202\\347%\\033\\3722\\326w\\030\\313\\v]{\\t`\\233t\\247\\320E\\350\\214\\361\\376\\fV\\343\\246rz?\\317\\314\\b\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\200\\302\\327/\\\"\\002\\bx\\272\\001\\000\"\n",
            txn.toProto().toString()
        );
    }
}
