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

import com.hedera.hashgraph.sdk.*;

import org.threeten.bp.Instant;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class ScheduleExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private ScheduleExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Generate a Ed25519 private, public key pair
        PrivateKey key1 = PrivateKey.generateED25519();
        PrivateKey key2 = PrivateKey.generateED25519();

        System.out.println("private key 1 = " + key1);
        System.out.println("public key 1 = " + key1.getPublicKey());
        System.out.println("private key 2 = " + key2);
        System.out.println("public key 2 = " + key2.getPublicKey());

        AccountId newAccountId = new AccountCreateTransaction()
            .setKey(KeyList.of(key1.getPublicKey(), key2.getPublicKey()))
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(newAccountId);

        System.out.println("new account = " + newAccountId);

        TransactionResponse response = new TransferTransaction()
            .addHbarTransfer(newAccountId, Hbar.from(1).negated())
            .addHbarTransfer(client.getOperatorAccountId(), Hbar.from(1))
            .schedule()
            // Set expiration time to be now + 24 hours
            .setExpirationTime(Instant.now().plusSeconds(24 * 60 * 60))
            // Set wait for expiry to true
            .setWaitForExpiry(true)
            .execute(client);

        System.out.println("scheduled transaction ID = " + response.transactionId);

        ScheduleId scheduleId = Objects.requireNonNull(response.getReceipt(client).scheduleId);
        System.out.println("schedule ID = " + scheduleId);

        TransactionRecord record = response.getRecord(client);
        System.out.println("record = " + record);

        new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(key1)
            .execute(client)
            .getReceipt(client);

        ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);

        System.out.println("schedule info = " + info);

        new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(key2)
            .execute(client)
            .getReceipt(client);

        TransactionId transactionId = response.transactionId;
        String validMirrorTransactionId = transactionId.accountId.toString() + "-" + transactionId.validStart.getEpochSecond() + "-" + transactionId.validStart.getNano();


        System.out.println("The following link should query the mirror node for the scheduled transaction");

        System.out.println("https://" + HEDERA_NETWORK + ".mirrornode.hedera.com/api/v1/transactions/" + validMirrorTransactionId);
    }
}
