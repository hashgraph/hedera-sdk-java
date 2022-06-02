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
import com.hedera.hashgraph.sdk.AccountBalance;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfo;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class AccountAliasExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Hedera supports a form of lazy account creation.
         *
         * You can "create" an account by generating a private key, and then deriving the public key,
         * without any need to interact with the Hedera network.  The public key more or less acts as the user's
         * account ID.  This public key is an account's aliasKey: a public key that aliases (or will eventually alias)
         * to a Hedera account.
         *
         * An AccountId takes one of two forms: a normal AccountId with a null aliasKey member takes the form 0.0.123,
         * while an account ID with a non-null aliasKey member takes the form
         * 0.0.302a300506032b6570032100114e6abc371b82dab5c15ea149f02d34a012087b163516dd70f44acafabf7777
         * Note the prefix of "0.0." indicating the shard and realm.  Also note that the aliasKey is stringified
         * as a hex-encoded ASN1 DER representation of the key.
         *
         * An AccountId with an aliasKey can be used just like a normal AccountId for the purposes of queries and
         * transactions, however most queries and transactions involving such an AccountId won't work until Hbar has
         * been transferred to the aliasKey account.
         *
         * There is no record in the Hedera network of an account associated with a given aliasKey
         * until an amount of Hbar is transferred to the account.  The moment that Hbar is transferred to that aliasKey
         * AccountId is the moment that that account actually begins to exist in the Hedera ledger.
         */

        System.out.println("\"Creating\" a new account");

        PrivateKey privateKey = PrivateKey.generateED25519();
        PublicKey publicKey = privateKey.getPublicKey();

        // Assuming that the target shard and realm are known.
        // For now they are virtually always 0 and 0.
        AccountId aliasAccountId = publicKey.toAccountId(0, 0);

        System.out.println("New account ID: " + aliasAccountId);
        System.out.println("Just the aliasKey: " + aliasAccountId.aliasKey);

        /*
         * Note that no queries or transactions have taken place yet.
         * This account "creation" process is entirely local.
         *
         * AccountId.fromString() can construct an AccountId with an aliasKey.
         * It expects a string of the form 0.0.123 in the case of a normal AccountId, or of the form
         * 0.0.302a300506032b6570032100114e6abc371b82dab5c15ea149f02d34a012087b163516dd70f44acafabf7777
         * in the case of an AccountId with aliasKey.  Note the prefix of "0.0." to indicate the shard and realm.
         *
         * If the shard and realm are known, you may use PublicKey.fromString().toAccountId() to construct the
         * aliasKey AccountId
         */

        AccountId fromString = AccountId.fromString("0.0.302a300506032b6570032100114e6abc371b82dab5c15ea149f02d34a012087b163516dd70f44acafabf7777");

        AccountId fromKeyString = PublicKey
                .fromString("302a300506032b6570032100114e6abc371b82dab5c15ea149f02d34a012087b163516dd70f44acafabf7777")
                .toAccountId(0, 0);


        System.out.println("Transferring some Hbar to the new account");
        new TransferTransaction()
            .addHbarTransfer(OPERATOR_ID, new Hbar(10).negated())
            .addHbarTransfer(aliasAccountId, new Hbar(10))
            .execute(client)
            .getReceipt(client);

        AccountBalance balance = new AccountBalanceQuery()
            .setAccountId(aliasAccountId)
            .execute(client);

        System.out.println("Balances of the new account: " + balance);

        AccountInfo info = new AccountInfoQuery()
            .setAccountId(aliasAccountId)
            .execute(client);

        System.out.println("Info about the new account: " + info);

        /*
         * Note that once an account exists in the ledger, it is assigned a normal AccountId, which can be retrieved
         * via an AccountInfoQuery.
         *
         * Users may continue to refer to the account by its aliasKey AccountId, but they may also
         * now refer to it by its normal AccountId
         */

        System.out.println("The normal account ID: " + info.accountId);
        System.out.println("The alias key: " + info.aliasKey);

        System.out.println("Example complete!");
        client.close();
    }
}
