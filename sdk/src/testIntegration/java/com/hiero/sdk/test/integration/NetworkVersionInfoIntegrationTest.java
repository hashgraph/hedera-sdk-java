// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk.test.integration;

import com.hiero.sdk.NetworkVersionInfoQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NetworkVersionInfoIntegrationTest {
    @Test
    @DisplayName("Cannot query network version info")
    void cannotQueryNetworkVersionInfo() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            new NetworkVersionInfoQuery().execute(testEnv.client);
        }
    }
}
