package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileAppendTransactionTest {

    @Test
    @DisplayName("empty builder fails validation")
    void emptyBuilder() {
        assertEquals(
            "transaction builder failed validation:\n"
                + ".setTransactionId() required\n"
                + ".setNodeAccountId() required\n"
                + ".setFileId() required\n"
                + ".setContents() required",
            assertThrows(
                IllegalStateException.class,
                () -> new FileAppendTransaction(null).validate()
            ).getMessage());
    }

    @Test
    @DisplayName("correct transaction can be built")
    void correctBuilder() {
        final var now = Instant.ofEpochSecond(1554158542);
        final var key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final var txnId = new TransactionId(new AccountId(2), now);
        final var txn = new FileAppendTransaction(null)
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setFileId(new FileId(1, 2, 3))
            .setContents(new byte[]{1, 2, 3, 4})
            .sign(key)
            .toProto();

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"o\\367\\257~9\\020s\\335Nz!\\332\\324\\271\\004\\224#\\222\\225s\\005\\315\\3772WSQ\\036`\\361\\300\\302.\\236\\2161\\324v|\\351\\331|Hs\\307\\361\\343\\v\\370`\\375\\323\\332>\\350\\212\\240\\317{<\\353\\267\\222\\r\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bx\\202\\001\\016\\022\\006\\b\\001\\020\\002\\030\\003\\\"\\004\\001\\002\\003\\004\"\n",
            txn.toString()
        );

    }
}
