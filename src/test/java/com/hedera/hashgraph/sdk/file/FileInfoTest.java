package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hashgraph.proto.FileGetInfoResponse;
import com.hedera.hashgraph.proto.KeyList;
import com.hedera.hashgraph.proto.Response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileInfoTest {

    private final Ed25519PrivateKey privateKey = Ed25519PrivateKey.generate();
    private final Ed25519PublicKey publicKey = privateKey.getPublicKey();

    @Test
    @DisplayName("won't deserialize from the wrong kind of response")
    void incorrectResponse() {
        assertThrows(
            IllegalArgumentException.class,
            () -> FileInfo.fromResponse(Response.getDefaultInstance())
        );
    }

    @Test
    @DisplayName("requires at least one key")
    void doesntRequireKey() {
        final Response response = Response.newBuilder()
            .setFileGetInfo(FileGetInfoResponse.getDefaultInstance())
            .build();

        assertEquals(
            "`FileGetInfoResponse` missing keys",
            assertThrows(
                IllegalArgumentException.class,
                () -> FileInfo.fromResponse(response)
            ).getMessage());
    }

    @Test
    @DisplayName("deserializes from a correct response")
    void correct() {
        final Response response = Response.newBuilder()
            .setFileGetInfo(
                FileGetInfoResponse.newBuilder()
                    .setFileInfo(
                        FileGetInfoResponse.FileInfo.newBuilder()
                            .setSize(1024)
                            .setKeys(
                                KeyList.newBuilder().addKeys(publicKey.toKeyProto()))))
            .build();

        final FileInfo fileInfo = FileInfo.fromResponse(response);

        assertEquals(fileInfo.getFileId(), new FileId(0, 0, 0));
        assertFalse(fileInfo.isDeleted());
        assertEquals(fileInfo.getSize(), 1024);
        assertEquals(fileInfo.getKeys().get(0).toKeyProto(), publicKey.toKeyProto());
    }
}
