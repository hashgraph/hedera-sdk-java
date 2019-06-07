package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class AccountDeleteTransactionTest {

    @Test
    @DisplayName("empty builder fails validation")
    void emptyBuilder() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new AccountCreateTransaction().validate(),
            "transaction builder failed validation:\n" +
                ".setTransactionId() required\n" +
                ".setNodeAccountId() required\n" +
                ".setTransferAccountId() required\n" +
                ".setDeleteAccountId() required");
    }

    @Test
    @DisplayName("transaction can be built")
    void correctBuilder() {
        final var now = Instant.ofEpochSecond(1554158542);
        final var key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final var txnId = new TransactionId(new AccountId(2), now);
        final var txn = new AccountDeleteTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setTransferAccountId(new AccountId(4))
            .setDeleteAccountId(new AccountId(1))
            .sign(key).toProto();
        
        Assertions.assertEquals(
                "sigMap {\n" +
                "  sigPair {\n" +
                "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n" +
                "    ed25519: \"CB\\337\\205\\355[\\364\\302\\021d\\355\\000\\371\\306\\026[P\\244\\335@7+\\016\\333\\030M\\276\\274S\\022\\276!\\265\\372\\023\\342\\206\\002\\330H^4\\0206h\\227L\\242A\\016\\342\\025\\303c\\021\\030\\332\\372\\257\\231;o\\242\\b\"\n" +
                "  }\n" +
                "}\n" +
                "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxb\\b\\n\\002\\030\\004\\022\\002\\030\\001\"\n",
            txn.toString()
        );
    }

}
