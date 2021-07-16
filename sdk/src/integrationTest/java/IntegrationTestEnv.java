import com.google.common.collect.HashBiMap;
import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;

import java.sql.Time;
import java.util.*;
import javax.annotation.Nullable;
import java.util.concurrent.TimeoutException;

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

    private static Client createClient() {
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

    public static IntegrationTestEnv withOneNode() throws PrecheckStatusException, TimeoutException, InterruptedException {
        var client = createClient();

        var nodeId = new AccountCreateTransaction()
            .setKey(PrivateKey.generate())
            .execute(client)
            .nodeId;

        var inverseNetwork = HashBiMap.create(client.getNetwork()).inverse();
        var newNetwork = new HashMap<String, AccountId>();
        newNetwork.put(Objects.requireNonNull(inverseNetwork.get(nodeId)), nodeId);
        client.setNetwork(newNetwork);
        return new IntegrationTestEnv(client, client.getOperatorAccountId());
    }

    public static IntegrationTestEnv withTwoNodes() throws PrecheckStatusException, TimeoutException, InterruptedException {
        var client = createClient();

        var nodeId1 = new AccountCreateTransaction()
            .setKey(PrivateKey.generate())
            .execute(client)
            .nodeId;

        var nodeId2 = new AccountCreateTransaction()
            .setKey(PrivateKey.generate())
            .execute(client)
            .nodeId;

        var inverseNetwork = HashBiMap.create(client.getNetwork()).inverse();
        var newNetwork = new HashMap<String, AccountId>();
        newNetwork.put(Objects.requireNonNull(inverseNetwork.get(nodeId1)), nodeId1);
        newNetwork.put(Objects.requireNonNull(inverseNetwork.get(nodeId2)), nodeId2);
        client.setNetwork(newNetwork);
        return new IntegrationTestEnv(client, client.getOperatorAccountId());
    }

    public static IntegrationTestEnv forTokenTest() throws PrecheckStatusException, TimeoutException, InterruptedException {
        var client = createClient();
        var originalOperatorId = client.getOperatorAccountId();

        var key = PrivateKey.generate();
        var nodeId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(130))
            .setKey(key)
            .execute(client)
            .nodeId;

        var inverseNetwork = HashBiMap.create(client.getNetwork()).inverse();
        var newNetwork = new HashMap<String, AccountId>();
        newNetwork.put(Objects.requireNonNull(inverseNetwork.get(nodeId)), nodeId);
        client.setNetwork(newNetwork);
        return new IntegrationTestEnv(client, originalOperatorId);
    }

    public void cleanUpAndClose(
        @Nullable TokenId newTokenId,
        @Nullable AccountId newAccountId,
        @Nullable PrivateKey newAccountKey
    ) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
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

    public void cleanUpAndClose() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        cleanUpAndClose(null, null, null);
    }

    public void cleanUpAndClose(
        AccountId newAccountId,
        PrivateKey newAccountKey
    ) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        cleanUpAndClose(null, newAccountId, newAccountKey);
    }

    public void cleanUpAndClose(
        TokenId newTokenId
    ) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        cleanUpAndClose(newTokenId, null, null);
    }
}
