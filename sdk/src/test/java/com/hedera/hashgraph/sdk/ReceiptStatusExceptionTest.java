package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptStatusExceptionTest {

    @Test
    void shouldHaveMessage() {
        var validStart = Instant.ofEpochSecond(1554158542);
        var txId = new TransactionId(new AccountId(0, 0, 100), validStart);
        var txReceipt = TransactionReceipt.fromProtobuf(
            com.hedera.hashgraph.sdk.proto.TransactionReceipt
                .newBuilder()
                .setStatusValue(ResponseCodeEnum.INSUFFICIENT_TX_FEE_VALUE)
                .build());
        var e = new ReceiptStatusException(
            txId,
            txReceipt
        );

        assertThat(e.getMessage()).isEqualTo(
            "receipt for transaction 0.0.100@1554158542.0 raised status INSUFFICIENT_TX_FEE"
        );
    }
}
