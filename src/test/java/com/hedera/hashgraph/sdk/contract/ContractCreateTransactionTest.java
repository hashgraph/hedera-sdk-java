package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContractCreateTransactionTest {

    @Test
    @DisplayName("Empty builder fails validation")
    void emptyBuilder() {
        assertEquals(
            "transaction builder failed validation:\n" +
                ".setTransactionId() required\n" +
                ".setNodeAccountId() required\n" +
                ".setBytecodeFile() required",
            assertThrows(
                IllegalStateException.class,
                () -> new ContractCreateTransaction().validate()
            ).getMessage()
        );
    }

    @Test
    @DisplayName("correct transaction can be built")
    void correctBuilder() {
        final var now = Instant.ofEpochSecond(1554158542);
        final var key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final var txnId = new TransactionId(new AccountId(2), now);
        final var txn = new ContractCreateTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setBytecodeFile(new FileId(1, 2, 3))
            .setAdminKey(key.getPublicKey())
            .setGas(0)
            .setInitialBalance(1000)
            .setProxyAccountId(new AccountId(4))
            .setAutoRenewPeriod(Duration.ofHours(7))
            .setConstructorParams(new byte[] {10, 11, 12, 13, 25})
            .setShard(20)
            .setRealm(40)
            .setNewRealmAdminKey(key.getPublicKey())
            .sign(key)
            .toProto();

        assertEquals(
            "sigMap {\n" +
                "  sigPair {\n" +
                "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n" +
                "    ed25519: \"\\222&\\032\\345\\241)3{\\230\\244\\253m\\354\\214\\231G\\333Bn\\333\\234\\2160\\275\\274\\205=\\335\\242\\320\\3162\\031v\\233y\\353,\\261G\\205\\221\\303\\251U\\360\\351\\320\\322\\310\\032U\\247\\263r\\207\\300xLP\\302\\227\\265\\t\"\n" +
                "  }\n" +
                "}\n" +
                "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxBl\\n\\006\\b\\001\\020\\002\\030\\003\\032\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216(\\350\\a2\\002\\030\\004B\\004\\b\\360\\304\\001J\\005\\n\\v\\f\\r\\031R\\002\\b\\024Z\\002\\020(b\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n",
            txn.toString()
        );
    }
}
