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

        Assertions.assertEquals("cost for AccountBalanceQuery, of 3000000000 tℏ, without explicit payment is greater than the maximum allowed payment of 1500000000 tℏ",
            e.getMessage());
    }
}
