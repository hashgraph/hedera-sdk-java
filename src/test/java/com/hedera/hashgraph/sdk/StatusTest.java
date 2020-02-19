package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatusTest {
    @Test
    @DisplayName("Status can be constructed from any ResponseCode")
    void statusToResponseCode() {
        for (ResponseCodeEnum code : ResponseCodeEnum.values()) {
            // not an actual value we want to handle
            // this is what we're given if an unexpected value was decoded
            if (code == ResponseCodeEnum.UNRECOGNIZED) continue;

            Status status = Status.valueOf(code);
            Assertions.assertEquals(code.getNumber(), status.code);
        }
    }

    @Test
    @DisplayName("Status throws on Unrecognized")
    void statusUnrecognized() {
        Assertions.assertEquals(
            "network returned unrecognized response code; "
                + "your SDK may be out of date",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Status.valueOf(ResponseCodeEnum.UNRECOGNIZED))
                .getMessage()
        );
    }
}
