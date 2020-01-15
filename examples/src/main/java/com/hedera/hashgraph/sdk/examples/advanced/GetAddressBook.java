package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileId;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

/** Get the network address book for inspecting the node public keys, among other things */
public final class GetAddressBook {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private GetAddressBook() { }

    public static void main(String[] args) throws HederaStatusException, IOException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        final FileContentsQuery fileQuery = new FileContentsQuery()
            .setFileId(FileId.ADDRESS_BOOK);

        final long cost = fileQuery.getCost(client);
        System.out.println("file contents cost: " + cost);

        fileQuery.setMaxQueryPayment(new Hbar(1));

        final byte[] contents = fileQuery.execute(client);

        Files.copy(new ByteArrayInputStream(contents),
            FileSystems.getDefault().getPath("address-book.proto.bin"));
    }
}
