package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionReceiptTest {

    @Test
    @DisplayName("requires `TransactionGetReceipt`")
    void requiresTransactionGetReceipt() {
        final var response = Response.getDefaultInstance();

        assertThrows(
            IllegalArgumentException.class,
            () -> new TransactionReceipt(response),
            ""
        );
    }

    @Test
    @DisplayName("missing fields throw")
    void missingFieldsThrow() {
        final var response = Response.newBuilder()
            .setTransactionGetReceipt(
                TransactionGetReceiptResponse.getDefaultInstance()
            )
            .build();

        final var receipt = new TransactionReceipt(response);

        assertEquals(receipt.getStatus(), ResponseCodeEnum.OK);

        assertThrows(
            IllegalStateException.class,
            receipt::getAccountId,
            "receipt does not contain an account ID"
        );

        assertThrows(
            IllegalStateException.class,
            receipt::getContractId,
            "receipt does not contain a contract ID"
        );

        assertThrows(
            IllegalStateException.class,
            receipt::getFileId,
            "receipt does not contain a file ID"
        );
    }

    @Test
    @DisplayName("receipt with account ID")
    void receiptWithAccountId() {
        final var expectedAccountId = new AccountId(1, 2, 3);

        final var response = Response.newBuilder()
            .setTransactionGetReceipt(
                TransactionGetReceiptResponse.newBuilder()
                    .setReceipt(
                        com.hederahashgraph.api.proto.java.TransactionReceipt.newBuilder()
                            .setAccountID(expectedAccountId.toProto())
                    )
            ).build();


        final var receipt = new TransactionReceipt(response);

        assertEquals(receipt.getAccountId(), expectedAccountId);
    }
}
