package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaxQueryPaymentExceededExceptionTest {
    @Test
    void shouldHaveMessage() {
        var e = new MaxQueryPaymentExceededException(
            new AccountBalanceQuery(),
            new Hbar(30),
            new Hbar(15)
        );

        Assertions.assertEquals("cost for AccountBalanceQuery, of 30 ℏ, without explicit payment is greater than the maximum allowed payment of 15 ℏ",
            e.getMessage());
    }
}
