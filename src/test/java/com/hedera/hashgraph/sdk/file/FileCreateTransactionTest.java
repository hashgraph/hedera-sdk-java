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
                + ".setNodeAccountId() required",
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
            .setNewRealmAdminKey(key.publicKey)
            .setMaxTransactionFee(100_000)
            .build()
            .sign(key)
            .toProto();

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"\\313\\000\\255\\006\\375\\207R\\225Q\\025\\375Zi\\215\\303\\326\\021bm\\202X\\022\\032\\350\\234\\357\\\"\\226\\215\\233`\\323(\\024v\\006\\333\\274\\222e\\250\\216\\350o$S\\177\\350\\253DG\\203\\027/e`\\351S\\031\\n\\264\\202\\353\\016\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bx\\212\\001X\\022\\006\\b\\210\\251\\212\\345\\005\\032$\\n\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\\\"\\004\\001\\002\\003\\004:\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n",
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
            .setMaxTransactionFee(100_000)
            .build()
            .sign(key)
            .toProto();

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"\\\\\\272j2\\264W\\030\\2251\\306\\213@\\254\\231A\\214\\200\\315\\2115[\\17762\\362\\301\\322$\\252>M\\005\\a\\272s-\\205C\\217^\\367\\\\\\270\\t\\017\\324\\'\\262!f\\314e\\323\\260\\304\\363\\202b\\345qM\\357\\000\\r\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bx\\212\\001\\n\\022\\006\\b\\210\\251\\212\\345\\005\\032\\000\"\n",
            txn.toString()
        );
    }
}
