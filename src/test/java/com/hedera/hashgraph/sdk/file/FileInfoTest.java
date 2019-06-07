package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hashgraph.sdk.proto.FileGetInfoResponse;
import com.hedera.hashgraph.sdk.proto.KeyList;
import com.hedera.hashgraph.sdk.proto.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileInfoTest {

    private final Ed25519PrivateKey privateKey = Ed25519PrivateKey.generate();
    private final Ed25519PublicKey publicKey = privateKey.getPublicKey();

    @Test
    @DisplayName("won't deserialize from the wrong kind of response")
    void incorrectResponse() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new FileInfo(Response.getDefaultInstance())
        );
    }

    @Test
    @DisplayName("requires at least one key")
    void doesntRequireKey() {
        final var response = Response.newBuilder()
            .setFileGetInfo(FileGetInfoResponse.getDefaultInstance())
            .build();

        assertEquals(
            "`FileGetInfoResponse` missing keys",
            assertThrows(
                IllegalArgumentException.class,
                () -> new FileInfo(response)
            ).getMessage());
    }

    @Test
    @DisplayName("deserializes from a correct response")
    void correct() {
        final var response = Response.newBuilder()
            .setFileGetInfo(
                FileGetInfoResponse.newBuilder()
                    .setFileInfo(
                        FileGetInfoResponse.FileInfo.newBuilder()
                            .setSize(1024)
                            .setKeys(
                                KeyList.newBuilder().addKeys(publicKey.toKeyProto()))))
            .build();

        final var fileInfo = new FileInfo(response);

        assertEquals(fileInfo.getFileId(), new FileId(0, 0, 0));
        assertFalse(fileInfo.isDeleted());
        assertEquals(fileInfo.getSize(), 1024);
        assertEquals(fileInfo.getKeys().get(0).toKeyProto(), publicKey.toKeyProto());
    }
}
