package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.proto.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionReceiptQueryTest {
    static final Ed25519PrivateKey privateKey = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962");

    private static final AccountId NODE_ACCOUNT = new AccountId(3);
    private static final AccountId USER_ACCOUNT = new AccountId(1234);

    static final Transaction paymentTxn = new CryptoTransferTransaction()
        .setNodeAccountId(NODE_ACCOUNT)
        .setTransactionId(new TransactionId(USER_ACCOUNT, Instant.parse("2019-04-05T12:00:00Z")))
        .addSender(USER_ACCOUNT, 10000)
        .addRecipient(NODE_ACCOUNT, 10000)
        .sign(privateKey);

    static final TransactionReceiptQuery query = new TransactionReceiptQuery()
        .setTransactionId(new TransactionId(USER_ACCOUNT, Instant.parse("2019-04-05T11:00:00Z")))
        .setPayment(paymentTxn);

    static final Query builtQuery = query.inner.build();

    static final String queryString = "" +
        "transactionGetReceipt {\n" +
        "  header {\n" +
        "    payment {\n" +
        "      sigMap {\n" +
        "        sigPair {\n" +
        "          pubKeyPrefix: \"\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\"\n" +
        "          ed25519: \"\\304B\\017\\242d=\\273\\3439\\305\\034\\224\\203#\\\\\\261\\343fa\\002]\\351\\\\\\036\\326\\327\\v\\037\\324\\317~\\020\\2371O\\020j\\377]\\261\\300\\216\\377n\\210\\264\\204?\\320\\001<\\225\\035E\\263&\\244 \\017\\207/\\332\\355\\017\"\n" +
        "        }\n" +
        "      }\n" +
        "      bodyBytes: \"\\n\\r\\n\\006\\b\\300\\206\\235\\345\\005\\022\\003\\030\\322\\t\\022\\002\\030\\003\\030\\240\\215\\006\\\"\\002\\bxr\\027\\n\\025\\n\\t\\n\\003\\030\\322\\t\\020\\237\\234\\001\\n\\b\\n\\002\\030\\003\\020\\240\\234\\001\"\n" +
        "    }\n" +
        "  }\n" +
        "  transactionID {\n" +
        "    transactionValidStart {\n" +
        "      seconds: 1554462000\n" +
        "    }\n" +
        "    accountID {\n" +
        "      accountNum: 1234\n" +
        "    }\n" +
        "  }\n" +
        "}\n";


    @Test
    @DisplayName("correct query validates")
    void correctQueryValidates() {
        assertDoesNotThrow(query::validate);
    }


    @Test
    @DisplayName("incorrect query does not validate")
    void incorrectQueryDoesNotValidate() {
        final var query = new TransactionReceiptQuery();

        assertThrows(
            IllegalStateException.class,
            query::validate,
            "query builder failed validation:\n" +
                ".setTransactionId() required"
        );
    }

    @Test
    @DisplayName("query builds correctly")
    void queryBuildsCorrectly() {
        assertEquals(builtQuery.toString(), queryString);
    }
}
