/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2023 - 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk.test.integration;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TokenUnpauseTransaction;
import com.hedera.hashgraph.sdk.TokenWipeTransaction;
import com.hedera.hashgraph.sdk.TransferTransaction;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TokenUnpauseIntegrationTest {

    @Test
    @DisplayName("Can execute token unpause transaction")
    void canExecuteTokenUnpauseTransaction() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()) {

            var accountKey = PrivateKey.generateED25519();
            var testTokenAmount = 10;
            var accountId = new AccountCreateTransaction().setKey(accountKey).setInitialBalance(new Hbar(2))
                .execute(testEnv.client).getReceipt(testEnv.client).accountId;

            var tokenId = new TokenCreateTransaction().setTokenName("ffff").setTokenSymbol("F").setInitialSupply(1000000)
                .setDecimals(3).setTreasuryAccountId(testEnv.operatorId).setAdminKey(testEnv.operatorKey)
                .setPauseKey(testEnv.operatorKey).setWipeKey(testEnv.operatorKey).setFreezeDefault(false)
                .execute(testEnv.client).getReceipt(testEnv.client).tokenId;

            new TokenAssociateTransaction().setAccountId(accountId).setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client).sign(accountKey).execute(testEnv.client).getReceipt(testEnv.client);

            new TokenUnpauseTransaction().setTokenId(tokenId).freezeWith(testEnv.client).execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction().addTokenTransfer(tokenId, accountId, testTokenAmount)
                .addTokenTransfer(tokenId, testEnv.operatorId, -testTokenAmount).execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenWipeTransaction().setTokenId(tokenId).setAccountId(accountId).setAmount(testTokenAmount)
                .execute(testEnv.client).getReceipt(testEnv.client);

            new TokenDeleteTransaction().setTokenId(tokenId).execute(testEnv.client).getReceipt(testEnv.client);

            new AccountDeleteTransaction().setTransferAccountId(testEnv.operatorId).setAccountId(accountId)
                .freezeWith(testEnv.client).sign(accountKey).execute(testEnv.client).getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Cannot unpause with no token ID")
    void cannotUnpauseWithNoTokenId() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()) {

            assertThrows(PrecheckStatusException.class, () -> {
                new TokenUnpauseTransaction().execute(testEnv.client).getReceipt(testEnv.client);
            });

        }
    }
}
