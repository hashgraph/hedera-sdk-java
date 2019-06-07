package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptoTransferTransactionTest {

    @Test
    @DisplayName("empty builder fails validation")
    void emptyBuilder() {
        assertEquals(
            "transaction builder failed validation:\n" +
                ".setTransactionId() required\n" +
                ".setNodeAccountId() required\n" +
                "at least one transfer required",
            assertThrows(
                IllegalStateException.class,
                () -> new CryptoTransferTransaction().validate()
            ).getMessage()
        );
    }

    @Test
    @DisplayName("transaction can be built")
    void correctBuilder() {
        final var now = Instant.ofEpochSecond(1554158542);
        final var key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final var txnId = new TransactionId(new AccountId(2), now);
        final var txn = new CryptoTransferTransaction()
            .setTransactionId(txnId)
            .setNodeAccountId(new AccountId(2))
            .addSender(new AccountId(4), 800)
            .addRecipient(new AccountId(55), 400)
            .addTransfer(new AccountId(78), 400)
            .sign(key).toProto();
        
        assertEquals(
                "sigMap {\n" +
                "  sigPair {\n" +
                "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n" +
                "    ed25519: \"\\223\\347\\326q\\3477\\303\\307g+\\207\\252\\251N=},v\\337\\231i\\r\\201\\276Qn\\277\\227\\261\\257\\332\\201a\\003[\\320\\036\\310\\\"\\353\\0338@\\216\\331w\\302\\300\\260\\276\\221\\330g\\300~\\234z\\247\\331\\031\\032\\300\\026\\006\"\n" +
                "  }\n" +
                "}\n" +
                "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\002\\030\\240\\215\\006\\\"\\002\\bxr\\035\\n\\033\\n\\a\\n\\002\\030\\004\\020\\277\\f\\n\\a\\n\\002\\0307\\020\\240\\006\\n\\a\\n\\002\\030N\\020\\240\\006\"\n",
            txn.toString()
        );
    }
}
