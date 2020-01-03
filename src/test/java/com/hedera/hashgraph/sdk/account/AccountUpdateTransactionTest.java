package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.proto.Transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountUpdateTransactionTest {

    @Test
    @DisplayName("empty builder fails validation")
    void emptyBuilder() {
        assertEquals(
            "transaction builder failed local validation:\n"
                + ".setTransactionId() required\n"
                + ".setNodeAccountId() required\n"
                + ".setAccountForUpdate() required",
            assertThrows(
                IllegalStateException.class,
                () -> new AccountUpdateTransaction().validate()
            ).getMessage()
        );
    }

    @Test
    @DisplayName("transaction can be built")
    void correctBuilder() {
        final Instant now = Instant.ofEpochSecond(1554158542);
        final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final TransactionId txnId = TransactionId.withValidStart(new AccountId(2), now);
        final Transaction txn = new AccountUpdateTransaction()
            .setKey(key.getPublicKey())
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setAccountId(new AccountId(2))
            .setProxyAccount(new AccountId(3))
            .setSendRecordThreshold(5)
            .setReceiveRecordThreshold(6)
            .setAutoRenewPeriod(Duration.ofHours(10))
            .setExpirationTime(Instant.ofEpochSecond(1554158543))
            .setMaxTransactionFee(100_000)
            .build()
            .sign(key)
            .toProto();

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"\\037\\371\\353\\t\\224\\356\\202\\b\\236\\354\\334#\\250\\2341N\\237\\017#\\003u\\361&\\265\\256\\302\\352\\033\\206\\220y\\341V\\351jA\\300\\205\\225\\355?@\\221\\317\\025C\\032\\217\\220\\006\\356\\301\\234\\213\\200\\365Bh\\316\\f}]\\245\\004\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxzB\\022\\002\\030\\002\\032\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\\\"\\002\\030\\003B\\004\\b\\240\\231\\002J\\006\\b\\317\\247\\212\\345\\005Z\\002\\b\\005b\\002\\b\\006\"\n",
            txn.toString()
        );
    }
}
