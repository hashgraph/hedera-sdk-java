/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2022 - 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk.test.integration;

import com.hiero.sdk.AccountAllowanceApproveTransaction;
import com.hiero.sdk.AccountCreateTransaction;
import com.hiero.sdk.AccountDeleteTransaction;
import com.hiero.sdk.Hbar;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.TransactionId;
import com.hiero.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountAllowanceIntegrationTest {
    @Test
    @DisplayName("Can spend hbar allowance")
    void canSpendHbarAllowance() throws Throwable {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var aliceKey = PrivateKey.generateED25519();
            var aliceId = new AccountCreateTransaction()
                .setKey(aliceKey)
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            var bobKey = PrivateKey.generateED25519();
            var bobId = new AccountCreateTransaction()
                .setKey(bobKey)
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            Objects.requireNonNull(aliceId);
            Objects.requireNonNull(bobId);

            new AccountAllowanceApproveTransaction()
                .approveHbarAllowance(bobId, aliceId, new Hbar(10))
                .freezeWith(testEnv.client)
                .sign(bobKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var transferRecord = new TransferTransaction()
                .addHbarTransfer(testEnv.operatorId, new Hbar(5))
                .addApprovedHbarTransfer(bobId, new Hbar(5).negated())
                .setTransactionId(TransactionId.generate(aliceId))
                .freezeWith(testEnv.client)
                .sign(aliceKey)
                .execute(testEnv.client)
                .getRecord(testEnv.client);

            var transferFound = false;
            for (var transfer : transferRecord.transfers) {
                if (transfer.accountId.equals(testEnv.operatorId) && transfer.amount.equals(new Hbar(5))) {
                    transferFound = true;
                    break;
                }
            }
            assertThat(transferFound).isTrue();

            new AccountDeleteTransaction()
                .setAccountId(bobId)
                .setTransferAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client)
                .sign(bobKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }
}
