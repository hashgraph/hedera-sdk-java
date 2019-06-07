package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContractUpdateTransactionTest {

    @Test
    @DisplayName("empty builder fails validation")
    void emptyBuilder() {
        assertEquals(
            "transaction builder failed validation:\n"
                + ".setTransactionId() required\n"
                + ".setNodeAccountId() required\n"
                + ".setContractId() required",
            assertThrows(
                IllegalStateException.class,
                () -> new ContractUpdateTransaction().validate()
            ).getMessage());
    }

    @Test
    @DisplayName("correct transaction can be built")
    void correctBuilder() {
        final var now = Instant.ofEpochSecond(1554158542);
        final var key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final var txnId = new TransactionId(new AccountId(2), now);
        final var txn = new ContractUpdateTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setContractId(new ContractId(1, 2, 3))
            .sign(key)
            .toProto();
        
        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"e\\027\\305WIXu\\217xJ\\254`\\214y\\345m\\324\\323\\243\\332\\301\\025X\\236\\2310\\244\\225\\266\\230\\217\\364]\\276\\251 \\\\\\243\\'\\016\\023\\025MJ\\377E\\022\\274\\240\\363\\232z.\\315\\333\\375\\\"\\323Xi\\253\\3606\\b\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxJ\\b\\n\\006\\b\\001\\020\\002\\030\\003\"\n",
            txn.toString()
        );

    }
}
