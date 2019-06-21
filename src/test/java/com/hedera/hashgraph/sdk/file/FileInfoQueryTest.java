package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileInfoQueryTest {

    private static final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");

    @Test
    @DisplayName("empty builder fails validation")
    void emptyBuilder() {
        assertEquals(
            "query builder failed validation:\n" +
                ".setPayment() required\n" +
                ".setFileId() required",
            assertThrows(
                IllegalStateException.class,
                () -> new FileInfoQuery().validate()
            ).getMessage()
        );
    }

    @Test
    @DisplayName("correct query can be built")
    void correctBuilder() {
        final var query = new FileInfoQuery()
            .setPayment(
                new CryptoTransferTransaction(null)
                    .setTransactionId(new TransactionId(new AccountId(2), Instant.ofEpochSecond(1559868457)))
                    .setNodeAccountId(new AccountId(3))
                    .addSender(new AccountId(2), 10000)
                    .addRecipient(new AccountId(3), 10000)
                    .sign(key))
            .setFileId(new FileId(1, 2, 3));

        assertEquals(
            "fileGetInfo {\n"
                + "  header {\n"
                + "    payment {\n"
                + "      sigMap {\n"
                + "        sigPair {\n"
                + "          pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "          ed25519: \"\\362\\367\\330\\025\\v\\331@\\302\\222\\324\\346\\310!G\\026\\003\\'=\\223\\355\\222\\006\\315_3\\351\\232\\350\\027v\\376\\361\\211\\277\\312\\262\\203giZ\\003\\222\\345\\347!c}\\3631\\0263z\\325\\323\\272\\020\\344\\214qwM((\\017\"\n"
                + "        }\n"
                + "      }\n"
                + "      bodyBytes: \"\\n\\f\\n\\006\\b\\251\\350\\346\\347\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxr\\026\\n\\024\\n\\b\\n\\002\\030\\002\\020\\237\\234\\001\\n\\b\\n\\002\\030\\003\\020\\240\\234\\001\"\n"
                + "    }\n"
                + "  }\n"
                + "  fileID {\n"
                + "    shardNum: 1\n"
                + "    realmNum: 2\n"
                + "    fileNum: 3\n"
                + "  }\n"
                + "}\n",
            query.toProto().toString()
        );
    }
}
