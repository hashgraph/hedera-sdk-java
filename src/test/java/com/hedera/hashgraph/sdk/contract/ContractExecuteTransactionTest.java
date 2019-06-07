package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContractExecuteTransactionTest {

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
                () -> new ContractExecuteTransaction().validate()
            ).getMessage()
        );
    }

    @Test
    @DisplayName("correct transaction can be built")
    void correctBuilder() {
        final var now = Instant.ofEpochSecond(1554158542);
        final var key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final var txnId = new TransactionId(new AccountId(2), now);
        final var txn = new ContractExecuteTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setContractId(new ContractId(1, 2, 3))
            .setGas(10)
            .setAmount(1000)
            .setFunctionParameters(new byte[] {24, 43, 11})
            .sign(key)
            .toProto();
        
        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"\\242\\227\\251\\345\\213\\033\\376=;\\331\\242R\\212\\233\\023\\377\\023\\'\\262I\\354\\001\\366\\236\\237LW8Y\\3554\\342\\335\\316\\256g\\212\\216\\256X)\\277\\354\\263&cC\\034\\305\\210\\260x\\214\\\\\\305\\324\\032\\360\\230\\r\\023\\365\\244\\n\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bx:\\022\\n\\006\\b\\001\\020\\002\\030\\003\\020\\n\\030\\350\\a\\\"\\003\\030+\\v\"\n",
            txn.toString()
        );

    }
}
