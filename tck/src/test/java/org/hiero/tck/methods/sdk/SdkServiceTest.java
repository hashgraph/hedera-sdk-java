// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hiero.tck.methods.sdk.param.*;
import org.hiero.tck.methods.sdk.response.*;
import java.util.Optional;
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

        response = sdkService.reset();

        assertEquals("", response.getMessage());
        assertNull(sdkService.getClient());
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
}
