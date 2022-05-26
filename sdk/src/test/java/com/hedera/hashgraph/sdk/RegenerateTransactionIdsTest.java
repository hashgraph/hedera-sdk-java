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
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.Status;
import java8.util.function.Function;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RegenerateTransactionIdsTest {
    @Test
    void regeneratesTransactionIdsWhenTransactionExpiredIsReturned() throws PrecheckStatusException, TimeoutException, InterruptedException {
        var transactionIds = new HashSet<TransactionId>();
        AtomicInteger count = new AtomicInteger(0);

        var responses = List.of(
            TransactionResponse.newBuilder().setNodeTransactionPrecheckCode(ResponseCodeEnum.TRANSACTION_EXPIRED).build(),
            TransactionResponse.newBuilder().setNodeTransactionPrecheckCode(ResponseCodeEnum.TRANSACTION_EXPIRED).build(),
            TransactionResponse.newBuilder().setNodeTransactionPrecheckCode(ResponseCodeEnum.TRANSACTION_EXPIRED).build(),
            TransactionResponse.newBuilder().setNodeTransactionPrecheckCode(ResponseCodeEnum.OK).build()
        );

        var call = (Function<Object, Object>) o -> {
            try {
                var transaction = (Transaction) o;
                var signedTransaction = SignedTransaction.parseFrom(transaction.getSignedTransactionBytes());
                var transactionBody = TransactionBody.parseFrom(signedTransaction.getBodyBytes());
                var transactionId = TransactionId.fromProtobuf(transactionBody.getTransactionID());

                if (transactionIds.contains(transactionId)) {
                    return Status.Code.ABORTED.toStatus().asRuntimeException();
                }

                transactionIds.add(transactionId);

                return responses.get(count.getAndIncrement());
            } catch (Throwable e) {
                return new RuntimeException(e);
            }
        };

        List<Object> responses1 = List.of(
            call, call, call, call
        );

        try (var mocker = Mocker.withResponses(List.of(responses1))) {
            new FileCreateTransaction().execute(mocker.client);
        }
    }
}
