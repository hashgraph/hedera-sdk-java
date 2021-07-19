import com.google.common.collect.HashBiMap;
import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TokenId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class IntegrationTestEnv {
    public Client client;
    public PublicKey operatorKey;
    public AccountId operatorId;
    public static Random random = new Random();
    private AccountId originalOperatorId;

    private IntegrationTestEnv(Client client, AccountId originalOperatorId) {
        this.client = client;
        this.operatorKey = client.getOperatorPublicKey();
        operatorId = client.getOperatorAccountId();
        this.originalOperatorId = originalOperatorId;

        try {
            var operatorPrivateKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
            operatorKey = operatorPrivateKey.getPublicKey();
            operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));

            client.setOperator(operatorId, operatorPrivateKey);
        } catch (Exception e) {
        }

        assertNotNull(client.getOperatorAccountId());
        assertNotNull(client.getOperatorPublicKey());
    }

    private static Client createTestEnvClient() {
        if (System.getProperty("HEDERA_NETWORK").equals("previewnet")) {
            return Client.forPreviewnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("localhost")) {
            var network = new Hashtable<String, AccountId>();
            network.put("127.0.0.1:50213", new AccountId(3));
            network.put("127.0.0.1:50214", new AccountId(4));
            network.put("127.0.0.1:50215", new AccountId(5));

            return Client.forNetwork(network);
        } else {
            try {
                var client = Client.fromConfigFile(System.getProperty("CONFIG_FILE"));
                return client;
            } catch (Exception e) {
                return Client.forTestnet();
            }
        }
    }

    private static class TestEnvNodeGetter {
        private Client client;
        private int index = 0;
        private static final int MAX_ATTEMPTS = 10;
        private List<Map.Entry<String, AccountId>> nodes;

        public TestEnvNodeGetter(Client client) {
            this.client = client;
            nodes = new ArrayList<>(client.getNetwork().entrySet());
            Collections.shuffle(nodes);
        }

        public void nextNode(Map<String, AccountId> outMap) throws Exception {
            if(nodes.isEmpty()) {
                throw new IllegalStateException("IntegrationTestEnv needs another node, but there aren't enough nodes in client network");
            }
            @Var
            int attempts = 0;
            while(true) {
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
                } catch(Exception exc) {
                }
                index++;
                if(index >= nodes.size()) {
                    attempts++;
                    if(attempts >= MAX_ATTEMPTS) {
                        throw new Exception("Failed to find working node in " + nodes + " for IntegrationTestEnv within " + MAX_ATTEMPTS + " attempts.");
                    }
                    index = 0;
                }
            }
        }
    }

    public static IntegrationTestEnv withOneNode() throws Exception {
        var client = createTestEnvClient();
        var network = new HashMap<String, AccountId>();
        new TestEnvNodeGetter(client).nextNode(network);
        client.setNetwork(network);
        return new IntegrationTestEnv(client, client.getOperatorAccountId());
    }

    public static IntegrationTestEnv withTwoNodes() throws Exception {
        var client = createTestEnvClient();
        var network = new HashMap<String, AccountId>();
        var nodeGetter = new TestEnvNodeGetter(client);
        nodeGetter.nextNode(network);
        nodeGetter.nextNode(network);
        client.setNetwork(network);
        client.setMaxNodesPerTransaction(2);
        return new IntegrationTestEnv(client, client.getOperatorAccountId());
    }

    // A throwaway account is needed for token tests to prevent us from running up against the 1000 token associations per account limit

    public static IntegrationTestEnv withThrowawayAccount(int amount) throws Exception {
        var client = createTestEnvClient();
        var originalOperatorId = client.getOperatorAccountId();
        var key = PrivateKey.generate();
        var response = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(amount))
            .setKey(key)
            .execute(client);
        var nodeId = response.nodeId;
        client.setOperator(response.getReceipt(client).accountId, key);
        var inverseNetwork = HashBiMap.create(client.getNetwork()).inverse();
        var newNetwork = new HashMap<String, AccountId>();
        newNetwork.put(Objects.requireNonNull(inverseNetwork.get(nodeId)), nodeId);
        client.setNetwork(newNetwork);
        return new IntegrationTestEnv(client, originalOperatorId);
    }

    public static IntegrationTestEnv withThrowawayAccount() throws Exception {
        return withThrowawayAccount(20);
    }

    public void cleanUpAndClose(
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

        if(newAccountId != null) {
            new AccountDeleteTransaction()
                .setTransferAccountId(originalOperatorId)
                .setAccountId(newAccountId)
                .freezeWith(client)
                .sign(Objects.requireNonNull(newAccountKey))
                .execute(client)
                .getReceipt(client);
        }

        if(!operatorId.equals(originalOperatorId)) {
            new AccountDeleteTransaction()
                .setTransferAccountId(originalOperatorId)
                .setAccountId(operatorId)
                .execute(client)
                .getReceipt(client);
        }

        client.close();
    }

    public void cleanUpAndClose() throws Exception {
        cleanUpAndClose(null, null, null);
    }

    public void cleanUpAndClose(AccountId newAccountId, PrivateKey newAccountKey) throws Exception {
        cleanUpAndClose(null, newAccountId, newAccountKey);
    }

    public void cleanUpAndClose(TokenId newTokenId) throws Exception {
        cleanUpAndClose(newTokenId, null, null);
    }
}
