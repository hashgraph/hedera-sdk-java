package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileId;

import java.io.FileOutputStream;
import java.io.IOException;

public final class LoadAddressBook {
    private LoadAddressBook() { }

    public static void main(String[] args) throws HederaException, IOException {
        final var client = ExampleHelper.createHederaClient();
        final var fileQuery = new FileContentsQuery(client)
            .setFileId(new FileId(0, 0, 102));

        final var cost = fileQuery.requestCost();
        System.out.println("file contents cost: " + cost);

        fileQuery.setPaymentDefault(100_000);

        final var contents = fileQuery.execute().getFileContents().getContents();

        try (final var fos = new FileOutputStream("address-book.proto.bin")) {
            contents.newInput().transferTo(fos);
        }
    }
}
