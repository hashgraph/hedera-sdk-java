package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountAddClaimTransactionTest {
    private static final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");

    private static final AccountId account = new AccountId(1000);

    private static final byte[] hash = {1, 2, 2, 3, 3, 3};

    private final AccountAddClaimTransaction txn = new AccountAddClaimTransaction(null)
        .setNodeAccountId(new AccountId(3))
        .setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-08T07:04:00Z")))
        .setAccountId(account)
        .setHash(hash)
        .addKey(key.getPublicKey())
        .setMaxTransactionFee(100_000);

    @Test
    @DisplayName("collect transaction validates")
    void correctTransactionValidates() {
        assertDoesNotThrow((ThrowingSupplier<Transaction>) txn::build);
    }

    @Test
    @DisplayName("incorrect transaction does not validate")
    void incorrectTransaction() {
        assertEquals(
            "transaction builder failed local validation:\n"
                + ".setTransactionId() required\n"
                + ".setNodeAccountId() required\n"
                + ".setAccountId() required\n"
                + ".setHash() required\n"
                + ".addKey() required",
            assertThrows(
                IllegalStateException.class,
                () -> new AccountAddClaimTransaction(null).build()).getMessage());
    }

    @Test
    @DisplayName("transaction serializes correctly")
    void transactionSerializesCorrectly() {
        assertEquals(
            "sigMap {\n"
                + "  sigPair {\n"
                + "    pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n"
                + "    ed25519: \"\\004\\362\\274\\370\\034\\231\\025W\\016,T\\331\\231\\201}d\\217P\\217/\\\\]\\217-\\332\\357\\343\\221UA\\n\\3655\\236\\207\\344\\a\\205\\304\\354t\\215<\\331\\204U\\335[ha+\\341\\262\\357\\204\\271$\\273t\\365E\\035\\377\\001\"\n"
                + "  }\n"
                + "}\n"
                + "bodyBytes: \"\\n\\r\\n\\006\\b\\340\\344\\253\\345\\005\\022\\003\\030\\322\\t\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxR5\\0323\\n\\003\\030\\350\\a\\022\\006\\001\\002\\002\\003\\003\\003\\032$\\n\\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n",
            txn.sign(key).toProto().toString());
    }

}
