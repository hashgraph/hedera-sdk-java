package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileId;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/** Get the network address book for inspecting the node public keys, among other things */
public final class GetAddressBook {
    private GetAddressBook() { }

    public static void main(String[] args) throws HederaException, IOException {
        final Client client = ExampleHelper.createHederaClient();
        final FileContentsQuery fileQuery = new FileContentsQuery(client)
            .setFileId(FileId.ADDRESS_BOOK);

        final long cost = fileQuery.requestCost();
        System.out.println("file contents cost: " + cost);

        fileQuery.setPaymentDefault(100_000);

        final ByteString contents = fileQuery.execute().getFileContents().getContents();

        Files.copy(contents.newInput(),
            FileSystems.getDefault().getPath("address-book.proto.bin"));
    }
}
