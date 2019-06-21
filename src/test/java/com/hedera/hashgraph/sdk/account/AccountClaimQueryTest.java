package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountClaimQueryTest {

    private static final Ed25519PrivateKey key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");

    private static final AccountId userAccountId = new AccountId(1000);
    private static final AccountId nodeAccountId = new AccountId(3);

    private static final AccountClaimQuery query = new AccountClaimQuery()
        .setPayment(
            new CryptoTransferTransaction(null)
                .setTransactionId(new TransactionId(userAccountId, Instant.parse("2019-05-28T15:20:00Z")))
                .setNodeAccountId(nodeAccountId)
                .addSender(userAccountId, 10000)
                .addRecipient(nodeAccountId, 10000)
                .sign(key)
        )
        .setAccountId(new AccountId(1234));

    @Test
    @DisplayName("incorrect query does not validate")
    void incorrectQuery() {
        assertThrows(
            IllegalStateException.class,
            () -> new AccountClaimQuery().validate(),
            ""
        );
    }

    @Test
    @DisplayName("correct query validates")
    void correctQuery() {
        assertDoesNotThrow(query::validate);
    }

    @Test
    @DisplayName("query serializes correctly")
    void querySerializes() {
        assertEquals(
            query.toProto().toString(),
            "cryptoGetClaim {\n" +
                "  header {\n" +
                "    payment {\n" +
                "      sigMap {\n" +
                "        sigPair {\n" +
                "          pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n" +
                "          ed25519: \"\\345\\323\\253\\252\\225_9_\\237A\\017\\233\\246\\351\\027\\314I2\\026\\335\\004\\362\\212\\207\\nX\\334\\r\\317q\\322\\263\\t\\233\\307\\2225\\215\\336\\260%,\\250%\\316\\312\\017\\325\\331\\3359\\332\\224K3\\246\\235\\a\\255\\205Q\\275\\221\\t\"\n" +
                "        }\n" +
                "      }\n" +
                "      bodyBytes: \"\\n\\r\\n\\006\\b\\240\\243\\265\\347\\005\\022\\003\\030\\350\\a\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxr\\027\\n\\025\\n\\t\\n\\003\\030\\350\\a\\020\\237\\234\\001\\n\\b\\n\\002\\030\\003\\020\\240\\234\\001\"\n" +
                "    }\n" +
                "  }\n" +
                "  accountID {\n" +
                "    accountNum: 1234\n" +
                "  }\n" +
                "}\n"
        );
    }
}
