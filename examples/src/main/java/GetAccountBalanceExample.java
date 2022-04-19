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
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class GetAccountBalanceExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private GetAccountBalanceExample() {
    }

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Because AccountBalanceQuery is a free query, we can make it without setting an operator on the client.

        Hbar balance = new AccountBalanceQuery()
            .setAccountId(OPERATOR_ID)
            .execute(client)
            .hbars;

        System.out.println("balance = " + balance);
    }
}
