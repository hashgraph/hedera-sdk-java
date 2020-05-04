package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse;
import com.hedera.hashgraph.sdk.proto.Response;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class AccountInfoTest {
    private static final PrivateKey privateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromProtobuf() {
        Response response = Response.newBuilder()
            .setCryptoGetInfo(
                CryptoGetInfoResponse.newBuilder()
                    .setAccountInfo(CryptoGetInfoResponse.AccountInfo.newBuilder()
                        .setKey(privateKey.getPublicKey().toKeyProtobuf()))
            )
            .build();

        SnapshotMatcher.expect(AccountInfo.fromProtobuf(response.getCryptoGetInfo().getAccountInfo()).toString())
            .toMatchSnapshot();
    }
}
