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

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;

/**
 * Entity IDs, such as `TokenId` and `AccountId`, can be constructed from strings.
 * For example, the `AccountId.fromString(inputString)` static method will attempt to parse
 * the input string and construct the expected AccountId object, and will throw an
 * `IllegalArgumentException` if the string is incorrectly formatted.
 * <p>
 * From here on, we'll talk about methods on `accountId`, but equivalent methods exist
 * on every entity ID type.
 * <p>
 * `fromString()` expects the input to look something like this: "1.2.3-asdfg".
 * Here, 1 is the shard, 2 is the realm, 3 is the number, and "asdfg" is the checksum.
 * <p>
 * The checksum can be used to ensure that an entity ID was inputted correctly.
 * For example, if the string being parsed is from a config file, or from user input,
 * it could contain typos.
 * <p>
 * You can use `accountId.getChecksum()` to get the checksum of an `accountId` object that was constructed
 * using `fromString()`. This will be the checksum from the input string. `fromString()` will merely
 * parse the string and create an `AccountId` object with the expected shard, realm, num, and checksum
 * values. `fromString()` will NOT verify that the `AccountId` maps to a valid account on the Hedera
 * network, and it will not verify the checksum.
 * <p>
 * To verify a checksum, call `accountId.validateChecksum(client)`. If the checksum
 * is invalid, `validateChecksum()` will throw a BadEntityIdException, otherwise it will return normally.
 * <p>
 * The validity of a checksum depends on which network the client is connected to (e.g. `mainnet` or
 * `testnet` or `previewnet`).  For example, a checksum that is valid for a particular shard/realm/num
 * on `mainnet` will be `INVALID` for the same shard/realm/num on testnet.
 * <p>
 * As far as `fromString()` is concerned, the checksum is optional.
 * If you use `fromString()` to generate an `AccountId` from a string that does not include a checksum,
 * such as "1.2.3", `fromString()` will work, but a call to the `getChecksum()` method on the resulting
 * `AccountId` object will return `null`.
 * <p>
 * Generally speaking, `AccountId` objects can come from three places:
 * 1) `AccountId.fromString(inString)`
 * 2) `new AccountId(shard, realm, num)`
 * 3) From the result of a query.
 * <p>
 * In the first case, the `AccountId` object will have a checksum (`getChecksum()` will not return null) if
 * the input string included a checksum, and it will not have a checksum if the string did not
 * include a checksum.
 * <p>
 * In the second and third cases, the `AccountId` object will not have a checksum.
 * <p>
 * If you call `accountId.validateChecksum(client)` and accountId has no checksum to validate,
 * `validateChecksum()` will silently pass, and will not throw an exception.
 * <p>
 * `accountId.toString()` will stringify the account ID with no checksum,
 * `accountId.toStringWithChecksum(client)` will stringify the account ID with the correct checksum
 * for that shard/realm/num on the client's network.
 */
class ValidateChecksumExample {

    // See `.env.sample` in the `examples` folder root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Step 1:
         * Read an input and validate the checksum (manual).
         */
        System.out.println("An example of manual checksum validation:");

        Scanner inputScanner = new Scanner(System.in, Charset.defaultCharset().name());

        while (true) {
            try {
                System.out.print("Enter an account ID with checksum: ");
                String inString = inputScanner.nextLine();

                // Throws `IllegalArgumentException` if incorrectly formatted.
                AccountId id = AccountId.fromString(inString);

                System.out.println("The ID with no checksum is " + id.toString());
                System.out.println("The ID with the correct checksum is " + id.toStringWithChecksum(client));

                if (id.getChecksum() == null) {
                    System.out.println("You must enter a checksum.");
                    continue;
                }
                System.out.println("The checksum entered was " + id.getChecksum());

                // Throws `BadEntityIdException` if checksum is incorrect.
                id.validateChecksum(client);

                AccountBalance balance = new AccountBalanceQuery()
                    .setAccountId(id)
                    .execute(client);
                System.out.println(balance);

                // Exit the loop.
                break;

            } catch (IllegalArgumentException exc) {
                System.out.println(exc.getMessage());
            } catch (BadEntityIdException exc) {
                System.out.println(exc.getMessage());
                System.out.println(
                    "You entered " + exc.shard + "." + exc.realm + "." + exc.num + "-" + exc.presentChecksum +
                        ", the expected checksum was " + exc.expectedChecksum
                );
            }
        }

        /*
         * Step 2:
         * Read an input and validate the checksum (auto).
         *
         * It is also possible to perform automatic checksum validation.
         *
         * Automatic checksum validation is disabled by default, but it can be enabled with
         * `client.setAutoValidateChecksums(true)`. You can check whether automatic checksum
         * validation is enabled with `client.isAutoValidateChecksumsEnabled()`.
         *
         * When this feature is enabled, the `execute()` method of a transaction or query
         * will automatically check the validity of checksums on any IDs in the
         * transaction or query.  It will throw an `IllegalArgumentException` if an
         * invalid checksum is encountered.
         */
        System.out.println("An example of automatic checksum validation:");

        client.setAutoValidateChecksums(true);

        while (true) {
            try {
                System.out.print("Enter an account ID with checksum: ");

                AccountId id = AccountId.fromString(inputScanner.nextLine());

                if (id.getChecksum() == null) {
                    System.out.println("You must enter a checksum.");
                    continue;
                }

                AccountBalance balance = new AccountBalanceQuery()
                    .setAccountId(id)
                    .execute(client);

                System.out.println(balance);

                // Exit the loop.
                break;

            } catch (IllegalArgumentException exc) {
                System.out.println(exc.getMessage());
            }
        }

        /*
         * Clean up:
         */
        client.close();
        System.out.println("Example complete!");
    }
}
