package com.hedera.hashgraph.sdk.test.integration;

import static com.hedera.hashgraph.sdk.test.integration.IntegrationTestEnv.LOCAL_CONSENSUS_NODE_ACCOUNT_ID;
import static com.hedera.hashgraph.sdk.test.integration.IntegrationTestEnv.LOCAL_CONSENSUS_NODE_ENDPOINT;

import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoadIntegrationTest {

    @Test
    @DisplayName("Load test")
    void loadTest() throws Exception {
        var testEnv = new IntegrationTestEnv(1);
        var operatorPrivateKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
        var operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));

        var network = new HashMap<String, AccountId>();
        network.put(LOCAL_CONSENSUS_NODE_ENDPOINT, LOCAL_CONSENSUS_NODE_ACCOUNT_ID);

        int nThreads = 100;
        var executor = new ThreadPoolExecutor(nThreads, nThreads,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
        testEnv.client = Client.forTestnetWithExecutor(executor);
        testEnv.client.setOperator(operatorId, operatorPrivateKey);
        testEnv.client.setMaxAttempts(15);

        long startTime = System.currentTimeMillis();

        var runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    new AccountCreateTransaction()
                        .setKey(PrivateKey.generateED25519())
                        .execute(testEnv.client)
                        .getReceipt(testEnv.client);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                } catch (PrecheckStatusException e) {
                    throw new RuntimeException(e);
                } catch (ReceiptStatusException e) {
                    throw new RuntimeException(e);
                }

                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                System.out.println("Thread " + Thread.currentThread().getName() + " finished in " + executionTime + "ms");
            }
        };

        for (int i = 0; i < 100; i++) {
            new Thread(runnable, "worker" + i).start();
        }

        Thread.sleep(70000);
    }
}
