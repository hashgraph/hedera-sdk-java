package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountIdTest {
    @Test
    @DisplayName("using toBytes and fromBytes will produce the correct Id")
    void keyGenerates() throws InvalidProtocolBufferException {
        AccountId id = AccountId.fromString("0.0.5005");

        assertNotNull(id);
        assertNotNull(id.toBytes());

        byte[] idBytes = id.toBytes();
        AccountId newId = AccountId.fromBytes(idBytes);

        assertEquals(id, newId);
    }


}
