// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TransactionReceiptIntegrationTest {

    @Test
    @DisplayName("nextExchangeRate property is not null in TransactionReceipt")
    void nextExchangeRatePropertyIsNotNullInTransactionReceipt() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var transaction =
                    new AccountCreateTransaction().setKeyWithoutAlias(key).execute(testEnv.client);

            var receipt = transaction.getReceipt(testEnv.client);

            var nextExchangeRate = receipt.nextExchangeRate;
            assertThat(nextExchangeRate).isNotNull();
        }
    }
}
