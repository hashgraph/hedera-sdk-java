/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
package com.hedera.hashgraph.tck.methods.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hedera.hashgraph.tck.methods.sdk.param.SetupParams;
import com.hedera.hashgraph.tck.methods.sdk.response.SetupResponse;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SdkServiceTest {

    private SdkService sdkService = new SdkService();

    @Test
    void testSetup() throws Exception {
        // Given
        SetupParams params = new SetupParams(
                "0.0.2",
                "302e020100300506032b65700422042091132178e72057a1d7528025956fe39b0b847f200ab59b2fdd367017f3087137",
                Optional.of("127.0.0.1:50211"),
                Optional.of("0.0.3"),
                Optional.of("http://127.0.0.1:5551"));

        // When
        SetupResponse response = sdkService.setup(params);

        // Then
        assertEquals("Successfully setup custom client.", response.getMessage());
    }

    @Test
    void testSetupFail() {
        // Given
        SetupParams params = new SetupParams(
                "operatorAccountId",
                "operatorPrivateKey",
                Optional.of("nodeIp"),
                Optional.of("3asdf"),
                Optional.of("127.0.0.1:50211"));

        // then
        assertThrows(Exception.class, () -> sdkService.setup(params));
    }

    @Test
    void testReset() throws TimeoutException {
        // When
        SetupResponse response = sdkService.reset();

        // Then
        assertEquals("", response.getMessage());
        assertNull(sdkService.getClient());
    }
}
