package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatusTest {
    @Test
    @DisplayName("Status can be constructed from any ResponseCode")
    void statusToResponseCode() {
        for (ResponseCodeEnum code : ResponseCodeEnum.values()) {
            // not an actual value we want to handle
            // this is what we're given if an unexpected value was decoded
            if (code == ResponseCodeEnum.UNRECOGNIZED) continue;

            Status status = Status.valueOf(code);

            assertEquals(code.getNumber(), status.code.getNumber());
        }
    }

    @Test
    @DisplayName("Status throws on Unrecognized")
    void statusUnrecognized() {
        assertEquals(
            "network returned unrecognized response code; "
                + "your SDK may be out of date",
            assertThrows(
                IllegalArgumentException.class,
                () -> Status.valueOf(ResponseCodeEnum.UNRECOGNIZED))
                .getMessage()
        );
    }
}
