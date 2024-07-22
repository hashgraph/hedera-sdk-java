/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountRecordsQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRecordsIntegrationTest {
    @Test
    @DisplayName("Can query account records")
    void canQueryAccountRecords() throws Exception {
        var testEnv = new IntegrationTestEnv(1);
        var key = PrivateKey.generateED25519();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        new TransferTransaction()
            .addHbarTransfer(testEnv.operatorId, new Hbar(1).negated())
            .addHbarTransfer(accountId, new Hbar(1))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addHbarTransfer(testEnv.operatorId, new Hbar(1))
            .addHbarTransfer(accountId, new Hbar(1).negated())
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var records = new AccountRecordsQuery()
            .setAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        assertThat(records.isEmpty()).isFalse();

        testEnv.close(accountId, key);
    }
}
