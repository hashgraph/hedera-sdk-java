package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoGetInfoResponse;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountInfoTest {
    private static final Ed25519PrivateKey privateKey = Ed25519PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    @Test
    @DisplayName("won't deserialize from the wrong kind of response")
    void incorrectResponse() {
        assertThrows(
            IllegalArgumentException.class,
            () -> AccountInfo.fromResponse(Response.getDefaultInstance())
        );
    }

    @Test
    @DisplayName("requires a key")
    void requiresKey() {
        final Response response = Response.newBuilder()
            .setCryptoGetInfo(CryptoGetInfoResponse.getDefaultInstance())
            .build();

        assertThrows(
            IllegalArgumentException.class,
            () -> AccountInfo.fromResponse(response),
            "query response missing key"
        );
    }

    @Test
    @DisplayName("deserializes from a correct response")
    void correct() {
        final Response response = Response.newBuilder()
            .setCryptoGetInfo(
                CryptoGetInfoResponse.newBuilder()
                    .setAccountInfo(CryptoGetInfoResponse.AccountInfo.newBuilder()
                        .setKey(privateKey.publicKey.toKeyProto())))
            .build();

        final AccountInfo accountInfo = AccountInfo.fromResponse(response);

        assertEquals(accountInfo.accountId, new AccountId(0));
        assertEquals(accountInfo.contractAccountId, "");
        assertNull(accountInfo.proxyAccountId);
    }

}
