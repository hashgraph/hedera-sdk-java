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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class IntegrationTestEnv {
    public static Random random = new Random();

    public Client client;
    public PublicKey operatorKey;
    public AccountId operatorId;
    private AccountId originalOperatorId;

    public IntegrationTestEnv(int numberOfNodes) throws Exception {
        client = createTestEnvClient()
            .setMaxNodesPerTransaction(numberOfNodes);

        try {
            var operatorPrivateKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
            operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));
            operatorKey = operatorPrivateKey.getPublicKey();

            client.setOperator(operatorId, operatorPrivateKey);
        } catch (Exception e) {
        }

        operatorKey = client.getOperatorPublicKey();
        operatorId = client.getOperatorAccountId();
        originalOperatorId = operatorId;

        assertNotNull(client.getOperatorAccountId());
        assertNotNull(client.getOperatorPublicKey());

        var nodeGetter = new TestEnvNodeGetter(client);
        var network = new HashMap<String, AccountId>();
        for (@Var int i = 0; i < numberOfNodes; i++) {
            nodeGetter.nextNode(network);
        }
        client.setNetwork(network);
    }

    private static Client createTestEnvClient() throws Exception {
        if (System.getProperty("HEDERA_NETWORK").equals("previewnet")) {
            return Client.forPreviewnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("testnet")) {
            return Client.forTestnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("localhost")) {
            var network = new Hashtable<String, AccountId>();
            network.put("127.0.0.1:50213", new AccountId(3));
            network.put("127.0.0.1:50214", new AccountId(4));
            network.put("127.0.0.1:50215", new AccountId(5));

            return Client.forNetwork(network);
        } else if (!System.getProperty("CONFIG_FILE").equals("")) {
            try {
                return Client.fromConfigFile(System.getProperty("CONFIG_FILE"));
            } catch (Exception e) {
            }
        }
        throw new IllegalStateException("Failed to construct client for IntegrationTestEnv");
    }

    public IntegrationTestEnv useThrowawayAccount(Hbar initialBalance) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        var key = PrivateKey.generate();
        operatorKey = key.getPublicKey();
        operatorId = new AccountCreateTransaction()
            .setInitialBalance(initialBalance)
            .setKey(key)
            .execute(client)
            .getReceipt(client)
            .accountId;
        client.setOperator(operatorId, key);
        return this;
    }

    public IntegrationTestEnv useThrowawayAccount() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        return useThrowawayAccount(new Hbar(50));
    }

    public void close(
        @Nullable TokenId newTokenId,
        @Nullable AccountId newAccountId,
        @Nullable PrivateKey newAccountKey
    ) throws Exception {
        if (newTokenId != null) {
            new TokenDeleteTransaction()
                .setTokenId(newTokenId)
                .execute(client)
                .getReceipt(client);
        }

        if (newAccountId != null) {
            new AccountDeleteTransaction()
                .setTransferAccountId(originalOperatorId)
                .setAccountId(newAccountId)
                .freezeWith(client)
                .sign(Objects.requireNonNull(newAccountKey))
                .execute(client)
                .getReceipt(client);
        }

        if (!operatorId.equals(originalOperatorId)) {
            new AccountDeleteTransaction()
                .setTransferAccountId(originalOperatorId)
                .setAccountId(operatorId)
                .execute(client)
                .getReceipt(client);
        }

        client.close();
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
                    new AccountBalanceQuery()
                        .setNodeAccountIds(Collections.singletonList(node.getValue()))
                        .setMaxAttempts(1)
                        .setAccountId(client.getOperatorAccountId())
                        .execute(client);
                    nodes.remove(index);
                    outMap.put(node.getKey(), node.getValue());
                    return;
                } catch (Exception ignored) {
                    throw ignored;
                }
            }
            throw new Exception("Failed to find working node in " + nodes + " for IntegrationTestEnv");
        }
    }
}
