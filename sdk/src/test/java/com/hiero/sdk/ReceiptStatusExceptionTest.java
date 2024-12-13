package com.hiero.sdk;

import com.hiero.sdk.AccountId;
import com.hiero.sdk.ReceiptStatusException;
import com.hiero.sdk.TransactionId;
import com.hiero.sdk.TransactionReceipt;
import com.hiero.sdk.proto.ResponseCodeEnum;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptStatusExceptionTest {

    @Test
    void shouldHaveMessage() {
        var validStart = Instant.ofEpochSecond(1554158542);
        var txId = new TransactionId(new AccountId(0, 0, 100), validStart);
        var txReceipt = TransactionReceipt.fromProtobuf(
            com.hiero.sdk.proto.TransactionReceipt
                .newBuilder()
                .setStatusValue(ResponseCodeEnum.INSUFFICIENT_TX_FEE_VALUE)
                .build());
        var e = new ReceiptStatusException(
            txId,
            txReceipt
        );

        assertThat(e.getMessage()).isEqualTo(
            "receipt for transaction 0.0.100@1554158542.000000000 raised status INSUFFICIENT_TX_FEE"
        );
    }
}
