/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2023 Hedera Hashgraph, LLC
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
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class GetAccountBalanceExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private GetAccountBalanceExample() {
    }

    public static void main(String[] args)
        throws PrecheckStatusException, TimeoutException, InterruptedException, ReceiptStatusException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Because AccountBalanceQuery is a free query, we can make it without setting an operator on the client.

//        var response = new TokenCreateTransaction()
//            .setTokenName("t1t2")
//            .setTokenSymbol("T1T2")
//            .setDecimals(3)
//            .setInitialSupply(1000000)
//            .setTreasuryAccountId(OPERATOR_ID)
//            .setAdminKey(OPERATOR_KEY.getPublicKey())
//            .setFreezeKey(OPERATOR_KEY.getPublicKey())
//            .setWipeKey(OPERATOR_KEY.getPublicKey())
//            .setKycKey(OPERATOR_KEY.getPublicKey())
//            .setSupplyKey(OPERATOR_KEY.getPublicKey())
//            .freezeWith(client)
//            .execute(client);
//
//        TokenId tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);
//        System.out.println("token = " + tokenId);

        Hbar balance = new AccountBalanceQuery()
//            .setContractId(new ContractId(7435949))
            .setAccountId(OPERATOR_ID)

            .execute(client)
            .hbars;

        System.out.println("balance = " + balance);
    }
}
