import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TransferTransaction;
import java8.util.function.Function;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTestEnv {
    public Client client;
    public PublicKey operatorKey;
    public AccountId operatorId;
    private Client originalClient;

    @SuppressWarnings("EmptyCatch")
    public IntegrationTestEnv(int numberOfNodes) throws Exception {
        client = createTestEnvClient()
            .setMaxNodesPerTransaction(numberOfNodes);
        originalClient = client;

        try {
            var operatorPrivateKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
            operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));
            operatorKey = operatorPrivateKey.getPublicKey();

            client.setOperator(operatorId, operatorPrivateKey);
        } catch (RuntimeException ignored) {
        }

        operatorKey = client.getOperatorPublicKey();
        operatorId = client.getOperatorAccountId();

        assertThat(client.getOperatorAccountId()).isNotNull();
        assertThat(client.getOperatorPublicKey()).isNotNull();

        var nodeGetter = new TestEnvNodeGetter(client);
        var network = new HashMap<String, AccountId>();
        for (@Var int i = 0; i < numberOfNodes; i++) {
            nodeGetter.nextNode(network);
        }
        client.setNetwork(network);
    }

    @SuppressWarnings("EmptyCatch")
    private static Client createTestEnvClient() throws Exception {
        if (System.getProperty("HEDERA_NETWORK").equals("previewnet")) {
            return Client.forPreviewnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("testnet")) {
            return Client.forTestnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("localhost")) {
            var network = new HashMap<String, AccountId>();
            network.put("127.0.0.1:50213", new AccountId(3));
            network.put("127.0.0.1:50214", new AccountId(4));
            network.put("127.0.0.1:50215", new AccountId(5));

            return Client.forNetwork(network);
        } else if (!System.getProperty("CONFIG_FILE").equals("")) {
            try {
                return Client.fromConfigFile(System.getProperty("CONFIG_FILE"));
            } catch (Exception configFileException) {
                configFileException.printStackTrace();
            }
        }
        throw new IllegalStateException("Failed to construct client for IntegrationTestEnv");
    }

    public IntegrationTestEnv useThrowawayAccount(Hbar initialBalance) throws Exception {
        var key = PrivateKey.generateED25519();
        operatorKey = key.getPublicKey();
        operatorId = new AccountCreateTransaction()
            .setInitialBalance(initialBalance)
            .setKey(key)
            .execute(client)
            .getReceipt(client)
            .accountId;

        client = Client.forNetwork(originalClient.getNetwork());
        client.setMirrorNetwork(originalClient.getMirrorNetwork());
        client.setOperator(Objects.requireNonNull(operatorId), key);
        return this;
    }

    public IntegrationTestEnv useThrowawayAccount() throws Exception {
        return useThrowawayAccount(new Hbar(50));
    }

    public void close(
        @Nullable TokenId newTokenId,
        @Nullable AccountId newAccountId,
        @Nullable PrivateKey newAccountKey
    ) throws Exception {
        if (newAccountId != null) {
            wipeAccountHbars(newAccountId, newAccountKey);
        }

        if (!operatorId.equals(originalClient.getOperatorAccountId())) {
            var hbarsBalance = new AccountBalanceQuery()
                .setAccountId(operatorId)
                .execute(originalClient)
                .hbars;
            new TransferTransaction()
                .addHbarTransfer(operatorId, hbarsBalance.negated())
                .addHbarTransfer(Objects.requireNonNull(originalClient.getOperatorAccountId()), hbarsBalance)
                .freezeWith(originalClient)
                .signWithOperator(client)
                .execute(originalClient);
            client.close();
        }

        originalClient.close();
    }

    public void wipeAccountHbars(AccountId newAccountId, PrivateKey newAccountKey) throws Exception {
        var hbarsBalance = new AccountBalanceQuery()
            .setAccountId(newAccountId)
            .execute(originalClient)
            .hbars;
        new TransferTransaction()
            .addHbarTransfer(newAccountId, hbarsBalance.negated())
            .addHbarTransfer(Objects.requireNonNull(originalClient.getOperatorAccountId()), hbarsBalance)
            .freezeWith(originalClient)
            .sign(Objects.requireNonNull(newAccountKey))
            .execute(originalClient);
    }

    public void close() throws Exception {
        close(null, null, null);
    }

    public void close(AccountId newAccountId, PrivateKey newAccountKey) throws Exception {
        close(null, newAccountId, newAccountKey);
    }

    public void close(TokenId newTokenId) throws Exception {
        close(newTokenId, null, null);
    }

    private static class TestEnvNodeGetter {
        private Client client;
        @Var
        private int index = 0;
        private List<Map.Entry<String, AccountId>> nodes;

        public TestEnvNodeGetter(Client client) {
            this.client = client;
            nodes = new ArrayList<>(client.getNetwork().entrySet());
            Collections.shuffle(nodes);
        }

        public void nextNode(Map<String, AccountId> outMap) throws Exception {
            if (nodes.isEmpty()) {
                throw new IllegalStateException("IntegrationTestEnv needs another node, but there aren't enough nodes in client network");
            }
            for (; index < nodes.size(); index++) {
                var node = nodes.get(index);
                try {
                    new TransferTransaction()
                        .setNodeAccountIds(Collections.singletonList(node.getValue()))
                        .setMaxAttempts(1)
                        .addHbarTransfer(client.getOperatorAccountId(), Hbar.fromTinybars(1).negated())
                        .addHbarTransfer(AccountId.fromString("0.0.3"), Hbar.fromTinybars(1))
                        .execute(client)
                        .getReceipt(client);
                    nodes.remove(index);
                    outMap.put(node.getKey(), node.getValue());
                    return;
                } catch (Throwable ignored) {
                }
            }
            throw new Exception("Failed to find working node in " + nodes + " for IntegrationTestEnv");
        }
    }
}
