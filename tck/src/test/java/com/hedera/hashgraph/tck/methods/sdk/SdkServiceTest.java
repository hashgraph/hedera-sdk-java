package com.hedera.hashgraph.tck.methods.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hedera.hashgraph.tck.exception.HederaException;
import com.hedera.hashgraph.tck.methods.sdk.param.SetupParams;
import com.hedera.hashgraph.tck.methods.sdk.response.SetupResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SdkServiceTest {

    private SdkService sdkService = new SdkService();

    @Test
    void testSetup() throws HederaException {
        // Given
        SetupParams params = new SetupParams(
                "0.0.2",
                "302e020100300506032b65700422042091132178e72057a1d7528025956fe39b0b847f200ab59b2fdd367017f3087137",
                "127.0.0.1:50211",
                "3",
                "http://127.0.0.1:5551"
        );

        // When
        SetupResponse response = sdkService.setup(params);

        // Then
        assertEquals("Successfully setup custom client.", response.getMessage());
    }


    @Test
    void testSetupFail() throws HederaException {
        // Given
        SetupParams params = new SetupParams(
            "operatorAccountId",
            "operatorPrivateKey",
            "nodeIp",
            "3asdf",
            "127.0.0.1:50211"
        );

        // then
        assertThrows(HederaException.class, () -> sdkService.setup(params));
    }

    @Test
    void testReset() {
        // When
        SetupResponse response = sdkService.reset();

        // Then
        assertEquals("", response.getMessage());
        assertNull(sdkService.getClient());
    }
}
