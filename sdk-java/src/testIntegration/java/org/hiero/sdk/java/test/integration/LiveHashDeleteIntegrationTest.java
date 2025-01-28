// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java.test.integration;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Objects;
import org.bouncycastle.util.encoders.Hex;
import org.hiero.sdk.java.AccountCreateTransaction;
import org.hiero.sdk.java.Hbar;
import org.hiero.sdk.java.LiveHashDeleteTransaction;
import org.hiero.sdk.java.PrecheckStatusException;
import org.hiero.sdk.java.PrivateKey;
import org.hiero.sdk.java.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiveHashDeleteIntegrationTest {
    private static final byte[] HASH = Hex.decode(
            "100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002");

    @Test
    @DisplayName("Cannot delete live hash because it's not supported")
    void cannotDeleteLiveHashBecauseItsNotSupported() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                    .setKey(key)
                    .setInitialBalance(new Hbar(1))
                    .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            assertThatExceptionOfType(PrecheckStatusException.class)
                    .isThrownBy(() -> {
                        new LiveHashDeleteTransaction()
                                .setAccountId(accountId)
                                .setHash(HASH)
                                .execute(testEnv.client)
                                .getReceipt(testEnv.client);
                    })
                    .withMessageContaining(Status.NOT_SUPPORTED.toString());
        }
    }
}
