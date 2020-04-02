package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.proto.Transaction;
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
            "transaction builder failed local validation:\n" +
                "`.setNodeAccountId()` required or a client must be provided\n" +
                ".setTransactionId() required\n" +
                ".setBytecodeFile() required",
            assertThrows(
                IllegalStateException.class,
                () -> new ContractCreateTransaction().build(null)
            ).getMessage()
        );
    }

    @Test
    @DisplayName("correct transaction can be built")
    void correctBuilder() {
        final Instant now = Instant.ofEpochSecond(1554158542);
        final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");
        final TransactionId txnId = TransactionId.withValidStart(new AccountId(2), now);
        final Transaction txn = new ContractCreateTransaction()
            .setNodeAccountId(new AccountId(3))
            .setTransactionId(txnId)
            .setBytecodeFileId(new FileId(1, 2, 3))
            .setAdminKey(key.publicKey)
            .setGas(0)
            .setInitialBalance(1000)
            .setProxyAccountId(new AccountId(4))
            .setAutoRenewPeriod(Duration.ofHours(7))
            .setConstructorParams(new byte[]{10, 11, 12, 13, 25})
            .setMaxTransactionFee(100_000)
            .build(null)
            .sign(key)
            .toProto();

        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"?\\340B\\227\\337\\263\\031\\023\\331\\315:6H\\252\\272\\003f\\243\\331\\307\\302\\316\\220H?\\257\\034\\200\\032\\251(\\026x\\\"\\2009r\\\"\\317\\2357\\017\\024i\\332\\363tY\\342T\\255\\260\\244\\213\\312\\207|\\252\\206\\027Y\\205\\031\\t\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxB@\\n\\006\\b\\001\\020\\002\\030\\003\\032\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216(\\350\\a2\\002\\030\\004B\\004\\b\\360\\304\\001J\\005\\n\\v\\f\\r\\031\"\n",
            txn.toString());
    }
}
