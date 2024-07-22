/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2021 - 2024 Hedera Hashgraph, LLC
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

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.FeeSchedules;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FeeSchedulesTest {
    @Test
    @DisplayName("FeeSchedules (CurrentAndNextFeeSchedule) is fetched and parsed from file 0.0.111")
    void canFetchFeeSchedules() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        ByteString feeSchedulesBytes = new FileContentsQuery()
            .setFileId(new FileId(0, 0, 111))
            .execute(testEnv.client);

        FeeSchedules feeSchedules = FeeSchedules.fromBytes(feeSchedulesBytes.toByteArray());

        /*
         * Test whether the file 0.0.111 actually contains stuff
         */
        assertThat(feeSchedules.getCurrent()).isNotNull();

        testEnv.close();
    }
}
