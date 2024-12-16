// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.hiero.sdk.FeeSchedules;
import com.hiero.sdk.FileContentsQuery;
import com.hiero.sdk.FileId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FeeSchedulesTest {
    @Test
    @DisplayName("FeeSchedules (CurrentAndNextFeeSchedule) is fetched and parsed from file 0.0.111")
    void canFetchFeeSchedules() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            ByteString feeSchedulesBytes =
                    new FileContentsQuery().setFileId(new FileId(0, 0, 111)).execute(testEnv.client);

            FeeSchedules feeSchedules = FeeSchedules.fromBytes(feeSchedulesBytes.toByteArray());

            /*
             * Test whether the file 0.0.111 actually contains stuff
             */
            assertThat(feeSchedules.getCurrent()).isNotNull();
        }
    }
}
