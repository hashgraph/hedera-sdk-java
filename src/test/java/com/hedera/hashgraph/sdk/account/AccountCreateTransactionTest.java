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
        assertThrows(
            IllegalStateException.class,
            () -> new AccountCreateTransaction(null).validate(),
            "transaction builder failed validation:\n" +
                ".setTransactionId() required\n" +
                ".setNodeAccountId() required\n" +
                ".setKey() required"
        );
    }

    @Test
    @DisplayName("transaction can be built")
    void correctBuilder() {
        final Instant now = Instant.ofEpochSecond(1554158542);
        final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final Transaction txn = new AccountCreateTransaction(null)
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(new TransactionId(new AccountId(2), now))
            .setKey(key.getPublicKey())
            .setInitialBalance(450)
            .setProxyAccountId(new AccountId(1020))
            .setReceiverSignatureRequired(true)
            .sign(key);

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"\\357\\204n\\334\\222L\\305\\022\\312\\034\\021\\'\\324n\\201\\347+\\223w\\226\\261\\261I\\307\\fTj\\236\\213\\321?\\230\\2176\\326p\\232\\025\\207\\322\\244?\\230\\265R\\035\\177kp\\211\\342\\034\\316\\215\\260\\335Z\\267\\301\\2718\\362]\\b\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxZM\\n\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\\020\\302\\003\\032\\003\\030\\374\\a0\\377\\377\\377\\377\\377\\377\\377\\377\\1778\\377\\377\\377\\377\\377\\377\\377\\377\\177@\\001J\\005\\b\\320\\310\\341\\003R\\000Z\\000\"\n",
            txn.toProto().toString());
    }
}
