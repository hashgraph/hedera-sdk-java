package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileCreateTransactionTest {

    @Test
    @DisplayName("empty builder fails validation")
    void emptyBuilder() {
        assertEquals(
            "transaction builder failed local validation:\n"
                + ".setTransactionId() required\n"
                + ".setNodeAccountId() required\n"
                + "network currently requires files to have at least one key",
            assertThrows(
                IllegalStateException.class,
                () -> new FileCreateTransaction().validate()
            ).getMessage());
    }

    @Test
    @DisplayName("correct transaction can be built")
    void correctBuilder() {
        final Instant now = Instant.ofEpochSecond(1554158542);
        final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final TransactionId txnId = TransactionId.withValidStart(new AccountId(2), now);
        final Transaction txn = new FileCreateTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setContents(new byte[]{1, 2, 3, 4})
            .setExpirationTime(Instant.ofEpochSecond(1554158728))
            .addKey(key.publicKey)
            .setMaxTransactionFee(100_000)
            .build(null)
            .sign(key)
            .toProto();

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"M\\324\\257\\337U\\331\\016\\025Z\\310;-\\324/\\250e\\310\\203\\tH<\\241\\265-8C\\226H\\341\\2654d\\330\\005\\273k\\346\\360\\'\\225\\351\\204a\\340\\2600}[q\\243\\265Ef\\226\\243&\\334\\230#\\315{YS\\003\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bx\\212\\0014\\022\\006\\b\\210\\251\\212\\345\\005\\032$\\n\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\\\"\\004\\001\\002\\003\\004\"\n",
            txn.toString()
        );
    }

    @Test
    @DisplayName("empty file can be built")
    void emptyFile() {
        final Instant now = Instant.ofEpochSecond(1554158542);
        final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final TransactionId txnId = TransactionId.withValidStart(new AccountId(2), now);
        final Transaction txn = new FileCreateTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setExpirationTime(Instant.ofEpochSecond(1554158728))
            .addKey(key.publicKey)
            .setMaxTransactionFee(100_000)
            .build(null)
            .sign(key)
            .toProto();

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"\\231\\001+\\023\\332p\\276C\\370<\\222\\f\\215\\002\\307>Q0\\301\\245\\223_hw\\201h\\\"\\006aFpr&Jdb\\230\\261=\\213\\277\\266v\\335\\033_,\\366\\346\\376Z\\aC\\251k\\366\\023\\257\\3174\\335R+\\b\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bx\\212\\001.\\022\\006\\b\\210\\251\\212\\345\\005\\032$\\n\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n",
            txn.toString()
        );
    }
}
