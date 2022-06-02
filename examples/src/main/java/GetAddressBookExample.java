/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AddressBookQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.NodeAddressBook;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * Get the network address book for inspecting the node public keys, among other things
 */
public final class GetAddressBookExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private GetAddressBookExample() {
    }

    public static void main(String[] args) throws PrecheckStatusException, IOException, TimeoutException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        FileContentsQuery fileQuery = new FileContentsQuery()
            .setFileId(FileId.ADDRESS_BOOK);

        Hbar cost = fileQuery.getCost(client);
        System.out.println("file contents cost: " + cost);

        fileQuery.setMaxQueryPayment(new Hbar(1));

        ByteString contents = fileQuery.execute(client);

        Files.deleteIfExists(FileSystems.getDefault().getPath("address-book.proto.bin"));

        Files.copy(new ByteArrayInputStream(contents.toByteArray()),
            FileSystems.getDefault().getPath("address-book.proto.bin"));

        // NEW (Feb 25 2022): you can now fetch the address book for free from a mirror node with AddressBookQuery
        NodeAddressBook addressBook = new AddressBookQuery()
            .setFileId(FileId.ADDRESS_BOOK)
            .execute(client);
        System.out.println(addressBook);
    }
}
