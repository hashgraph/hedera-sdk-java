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
import com.hedera.hashgraph.sdk.AddressBookQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.NodeAddressBook;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * Get the network address book for inspecting the node public keys, among other things
 */
public final class GetAddressBookExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private GetAddressBookExample() {
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        // NEW (Feb 25 2022): you can now fetch the address book for free from a mirror node with AddressBookQuery

        Client client = Client.forName(HEDERA_NETWORK)
            .setTransportSecurity(true); // Mirror node queries like AddressBookQuery on mainnet require TLS

        NodeAddressBook addressBook = new AddressBookQuery()
            .setFileId(FileId.ADDRESS_BOOK)
            .execute(client);

        System.out.println(addressBook);

        Files.deleteIfExists(FileSystems.getDefault().getPath("address-book.proto.bin"));

        Files.copy(new ByteArrayInputStream(addressBook.toBytes().toByteArray()),
            FileSystems.getDefault().getPath("address-book.proto.bin"));
    }
}
