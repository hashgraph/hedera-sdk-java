package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse;
import com.hedera.hashgraph.sdk.proto.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccountInfoTest {
    private static final PrivateKey privateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    @Test
    @DisplayName("using toBytes and fromBytes will produce the correct response")
    void toFromBytes() throws InvalidProtocolBufferException {
        Response response = Response.newBuilder()
            .setCryptoGetInfo(
                CryptoGetInfoResponse.newBuilder()
                    .setAccountInfo(CryptoGetInfoResponse.AccountInfo.newBuilder()
                        .setKey(privateKey.getPublicKey().toKeyProtobuf()))
            )
            .build();

        AccountInfo info = AccountInfo.fromProtobuf(response.getCryptoGetInfo().getAccountInfo());

        assertNotNull(info);
        assertNotNull(info.toBytes());

        byte[] infoBytes = info.toBytes();
        AccountInfo newInfo = AccountInfo.fromBytes(infoBytes);

        assertEquals(info.toString(), newInfo.toString());
    }
}
