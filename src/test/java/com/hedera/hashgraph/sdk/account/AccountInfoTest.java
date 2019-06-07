package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse;
import com.hedera.hashgraph.sdk.proto.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountInfoTest {
    private static final Ed25519PrivateKey privateKey = Ed25519PrivateKey.generate();

    @Test
    @DisplayName("won't deserialize from the wrong kind of response" )
    void incorrectResponse() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new AccountInfo(Response.getDefaultInstance())
        );
    }

    @Test
    @DisplayName("requires a key" )
    void requiresKey() {
        final var response = Response.newBuilder()
            .setCryptoGetInfo((CryptoGetInfoResponse.getDefaultInstance()))
            .build();

        assertThrows(
            IllegalArgumentException.class,
            () -> new AccountInfo(response),
            "query response missing key"
        );
    }

    @Test
    @DisplayName("deserializes from a correct response" )
    void correct() {
        final var response = Response.newBuilder()
            .setCryptoGetInfo(
                CryptoGetInfoResponse.newBuilder()
                    .setAccountInfo(CryptoGetInfoResponse.AccountInfo.newBuilder()
                        .setKey(privateKey.getPublicKey().toKeyProto())))
            .build();

        final var accountInfo = new AccountInfo(response);

        assertEquals(accountInfo.getAccountId(), new AccountId(0));
        assertEquals(accountInfo.getContractAccountId(), "" );
        assertNull(accountInfo.getProxyAccountId());
        assertEquals(accountInfo.getClaims(), List.of());
    }

}
