package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminUndeleteTransactionTest {

    @Test
    @DisplayName("empty transaction does not validate")
    void emptyTransaction() {
        assertThrows(
            IllegalStateException.class,
            () -> new AdminUndeleteTransaction().validate(),
            "transaction builder failed validation:\n" +
                "setTransactionId() required\n" +
                ".setNodeAccountId() required\n" +
                ".setID() required"
        );
    }

    @Test
    @DisplayName("transaction can be built with FileId")
    void transactionWithFileId() {
        final var now = Instant.ofEpochSecond(1554158542);
        final var key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final var txId = new TransactionId(new AccountId(2), now);
        final var txn = new AdminUndeleteTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txId)
            .setID(new FileId(1, 2, 3))
            .sign(key)
            .toProto();

        assertEquals(
            txn.toString(),
            "sigMap {\n" +
                "  sigPair {\n" +
                "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n" +
                "    ed25519: \"<\\233k\\333)\\307%|\\325#\\365\\b\\253\\016@\\377\\371\\004\\312\\266\\022\\266\\001\\2449\\373\\310r\\251\\372\\2347\\270\\225\\342\\221y\\3533\\325\\377\\206\\361\\333$E\\304\\026yl\\357\\371\\030\\342\\220D\\226#Y2\\027\\002R\\n\"\n" +
                "  }\n" +
                "}\n" +
                "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bx\\252\\001\\b\\n\\006\\b\\001\\020\\002\\030\\003\"\n"
        );
    }

    @Test
    @DisplayName("transaction can be built with ContractId")
    void transactionWithContractId() {
        final var now = Instant.ofEpochSecond(1554158542);
        final var key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final var txId = new TransactionId(new AccountId(2), now);
        final var txn = new AdminUndeleteTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txId)
            .setID(new ContractId(1, 2, 3))
            .sign(key)
            .toProto();

        assertEquals(
            txn.toString(),
            "sigMap {\n" +
                "  sigPair {\n" +
                "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n" +
                "    ed25519: \"\\020;db\\032\\271\\374\\370(9\\326\\002}\\321\\342\\271\\2375\\372\\374\\366\\334N\\270eA\\271\\247*\\217\\037A\\r\\377\\003.\\352Y\\265$^\\353ZTd\\376\\036\\235\\315\\330\\0335Ya-\\bn\\021?e\\237\\rX\\v\"\n" +
                "  }\n" +
                "}\n" +
                "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bx\\252\\001\\b\\022\\006\\b\\001\\020\\002\\030\\003\"\n"
        );
    }
}
