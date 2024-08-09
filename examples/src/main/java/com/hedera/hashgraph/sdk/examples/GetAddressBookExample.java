/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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
package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.AddressBookQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.NodeAddressBook;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * How to get the network address book.
 * Also, how to inspect node public keys, etc.
 */
class GetAddressBookExample {

    // See `.env.sample` in the `examples` folder root for how to specify these values
    // or set environment variables with the same names
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        /*
         * Step 1:
         * Fetch the address book.
         * Note: from Feb 25 2022 you can now fetch the address book for free from a mirror node with `AddressBookQuery`.
         */
        System.out.println("Getting address book for " + HEDERA_NETWORK);

        NodeAddressBook addressBook = new AddressBookQuery()
            .setFileId(FileId.ADDRESS_BOOK)
            .execute(client);

        System.out.println(addressBook);

        /*
         * Clean up:
         */
        Files.deleteIfExists(FileSystems.getDefault().getPath("address-book.proto.bin"));
        client.close();

        System.out.println("Example complete!");
    }
}
