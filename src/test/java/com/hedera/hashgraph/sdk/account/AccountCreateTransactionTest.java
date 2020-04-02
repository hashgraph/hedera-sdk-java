package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountCreateTransactionTest {

    @Test
    @DisplayName("empty builder fails validation")
    void emptyBuilder() {
        assertEquals(
            "transaction builder failed local validation:\n" +
                "`.setNodeAccountId()` required or a client must be provided\n" +
                ".setTransactionId() required\n" +
                ".setKey() required",
            assertThrows(
                IllegalStateException.class,
                () -> new AccountCreateTransaction().build(null)).getMessage()
        );
    }

    @Test
    @DisplayName("transaction can be built")
    void correctBuilder() {
        final Instant now = Instant.ofEpochSecond(1554158542);
        final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final Transaction txn = new AccountCreateTransaction()
                .setNodeAccountId(new AccountId(3))
                .setTransactionId(TransactionId.withValidStart(new AccountId(2), now))
                .setKey(key.publicKey)
                .setInitialBalance(450)
                .setProxyAccountId(new AccountId(1020))
                .setReceiverSignatureRequired(true)
                .setMaxTransactionFee(100_000).build(null)
            .sign(key);

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"\\256Y\\t\\2075\\314G\\340\\261&\\222\\241\\365\\245\\375\\n2\\254\\206\\260e\\275v\\022LD\\233\\203\\001\\302WO\\365g\\332\\237\\306\\331\\177\\341\\371\\341\\037`{\\002\\207*\\352\\367\\000\\306\\312\\363w\\230\\3377R\\002\\022\\350v\\a\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxZI\\n\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\\020\\302\\003\\032\\003\\030\\374\\a0\\377\\377\\377\\377\\377\\377\\377\\377\\1778\\377\\377\\377\\377\\377\\377\\377\\377\\177@\\001J\\005\\b\\320\\310\\341\\003\"\n",
            txn.toProto().toString());
    }
}
